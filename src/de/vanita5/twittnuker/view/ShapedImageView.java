/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An ImageView class with a circle mask so that all images are drawn in a
 * circle instead of a square.
 */
public class ShapedImageView extends ImageView implements Constants {


    @IntDef({SHAPE_CIRCLE, SHAPE_RECTANGLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShapeStyle {
    }

    @ShapeStyle
    public static final int SHAPE_CIRCLE = 0x1;
    @ShapeStyle
    public static final int SHAPE_RECTANGLE = 0x2;

	private static final int SHADOW_START_COLOR = 0x37000000;

    private static final boolean USE_OUTLINE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	private static final boolean OUTLINE_DRAW = false;

	private final Matrix mMatrix;
	private final RectF mSource;
	private final RectF mDestination;
    private final RectF mTempDestination;
	private final Paint mBitmapPaint;
	private final Paint mBorderPaint;
    private final Paint mBackgroundPaint;

	private boolean mBorderEnabled;
	private Bitmap mShadowBitmap;
	private float mShadowRadius;

	private int mStyle;
	private float mCornerRadius, mCornerRadiusRatio;

	public ShapedImageView(Context context) {
		this(context, null, 0);
	}

	public ShapedImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

    @Override
    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(0xFF000000 | color);
        mBackgroundPaint.setAlpha(Color.alpha(color));
        invalidate();
    }

	public ShapedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShapedImageView, defStyle, 0);

		mMatrix = new Matrix();
		mSource = new RectF();
		mDestination = new RectF();
        mTempDestination = new RectF();

        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBitmapPaint.setFilterBitmap(true);
		mBitmapPaint.setDither(true);

		mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBorderPaint.setStyle(Paint.Style.STROKE);

        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        if (a.hasValue(R.styleable.ShapedImageView_sivBorder)) {
            setBorderEnabled(a.getBoolean(R.styleable.ShapedImageView_sivBorder, false));
        } else if (a.hasValue(R.styleable.ShapedImageView_sivBorderColor)
                || a.hasValue(R.styleable.ShapedImageView_sivBorderWidth)) {
			setBorderEnabled(true);
		}
        setBorderColor(a.getColor(R.styleable.ShapedImageView_sivBorderColor, Color.TRANSPARENT));
        setBorderWidth(a.getDimensionPixelSize(R.styleable.ShapedImageView_sivBorderWidth, 0));
        @ShapeStyle
        final int shapeStyle = a.getInt(R.styleable.ShapedImageView_sivShape, SHAPE_RECTANGLE);
        setStyle(shapeStyle);
        setCornerRadius(a.getDimension(R.styleable.ShapedImageView_sivCornerRadius, 0));
        setCornerRadiusRatio(a.getFraction(R.styleable.ShapedImageView_sivCornerRadiusRatio, 1, 1, -1));

		if (USE_OUTLINE) {
            if (a.hasValue(R.styleable.ShapedImageView_sivElevation)) {
				ViewCompat.setElevation(this,
                        a.getDimensionPixelSize(R.styleable.ShapedImageView_sivElevation, 0));
			}
		} else {
            mShadowRadius = a.getDimensionPixelSize(R.styleable.ShapedImageView_sivElevation, 0);
		}
        setBackgroundColor(a.getColor(R.styleable.ShapedImageView_sivBackgroundColor, 0));
		a.recycle();

		if (USE_OUTLINE) {
			initOutlineProvider();
		}
	}

    public void setCornerRadiusRatio(float ratio) {
        mCornerRadiusRatio = ratio;
	}

    /**
     * Given the source bitmap and a canvas, draws the bitmap through a circular
     * mask. Only draws a circle with diameter equal to the destination width.
     *
     * @param bitmap The source bitmap to draw.
     * @param canvas The canvas to draw it on.
     * @param source The source bound of the bitmap.
     * @param dest   The destination bound on the canvas.
     */
    public void drawBitmapWithCircleOnCanvas(Bitmap bitmap, Canvas canvas,
                                             RectF source, RectF dest) {
        // Draw bitmap through shader first.
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        mMatrix.reset();

        switch (getScaleType()) {
            case CENTER_CROP: {
                final float srcRatio = source.width() / source.height();
                final float dstRatio = dest.width() / dest.height();
                if (srcRatio > dstRatio) {
                    // Source is wider than destination, fit height
                    mTempDestination.top = dest.top;
                    mTempDestination.bottom = dest.bottom;
                    final float dstWidth = dest.height() * srcRatio;
                    mTempDestination.left = dest.centerX() - dstWidth / 2;
                    mTempDestination.right = dest.centerX() + dstWidth / 2;
                } else if (srcRatio < dstRatio) {
                    mTempDestination.left = dest.left;
                    mTempDestination.right = dest.right;
                    final float dstHeight = dest.width() / srcRatio;
                    mTempDestination.top = dest.centerY() - dstHeight / 2;
                    mTempDestination.bottom = dest.centerY() + dstHeight / 2;
			    } else {
                    mTempDestination.set(dest);
			    }
                break;
		    }
            default: {
                mTempDestination.set(dest);
                break;
	        }
	    }

        // Fit bitmap to bounds.
        mMatrix.setRectToRect(source, mTempDestination, ScaleToFit.CENTER);

        shader.setLocalMatrix(mMatrix);
        mBitmapPaint.setShader(shader);

        if (getStyle() == SHAPE_CIRCLE) {
            canvas.drawCircle(dest.centerX(), dest.centerY(), Math.min(dest.width(), dest.height()) / 2f,
                    mBitmapPaint);
        } else {
            final float cornerRadius = getCalculatedCornerRadius();
            canvas.drawRoundRect(dest, cornerRadius, cornerRadius, mBitmapPaint);
	    }
    }

    @ShapeStyle
    public int getStyle() {
        return mStyle;
	}

    public void setStyle(@ShapeStyle final int style) {
        mStyle = style;
	}

    public void setBorderColor(int color) {
        mBorderPaint.setARGB(Color.alpha(color), Color.red(color), Color.green(color),
                Color.blue(color));
        invalidate();
    }

    public void setBorderEnabled(boolean enabled) {
        mBorderEnabled = enabled;
        invalidate();
	}

    public void setBorderWidth(int width) {
        mBorderPaint.setStrokeWidth(width);
        invalidate();
    }

	@Override
	protected void onDraw(@NonNull Canvas canvas) {

		mDestination.set(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(),
				getHeight() - getPaddingBottom());

        if (getStyle() == SHAPE_CIRCLE) {
            canvas.drawOval(mDestination, mBackgroundPaint);
        } else {
            final float radius = getCalculatedCornerRadius();
            canvas.drawRoundRect(mDestination, radius, radius, mBackgroundPaint);
        }

        if (OUTLINE_DRAW) {
			super.onDraw(canvas);
		} else {
			final int contentLeft = getPaddingLeft(), contentTop = getPaddingTop(),
					contentRight = getWidth() - getPaddingRight(),
					contentBottom = getHeight() - getPaddingBottom();
			final int contentWidth = contentRight - contentLeft,
					contentHeight = contentBottom - contentTop;
			final int size = Math.min(contentWidth, contentHeight);
			if (mShadowBitmap != null) {
				canvas.drawBitmap(mShadowBitmap, contentLeft + (contentWidth - size) / 2 - mShadowRadius,
						contentTop + (contentHeight - size) / 2 - mShadowRadius, null);
			}
			Drawable drawable = getDrawable();
			BitmapDrawable bitmapDrawable = null;
			// support state list drawable by getting the current state
			if (drawable instanceof StateListDrawable) {
				if (drawable.getCurrent() != null) {
					bitmapDrawable = (BitmapDrawable) drawable.getCurrent();
				}
			} else {
				bitmapDrawable = (BitmapDrawable) drawable;
			}

			if (bitmapDrawable == null) {
				return;
			}
			Bitmap bitmap = bitmapDrawable.getBitmap();
			if (bitmap == null) {
				return;
			}

			mSource.set(0, 0, bitmap.getWidth(), bitmap.getHeight());

			drawBitmapWithCircleOnCanvas(bitmap, canvas, mSource, mDestination);
		}

		// Then draw the border.
		if (mBorderEnabled) {
            if (getStyle() == SHAPE_CIRCLE) {
                canvas.drawCircle(mDestination.centerX(), mDestination.centerY(),
                        mDestination.width() / 2f - mBorderPaint.getStrokeWidth() / 2, mBorderPaint);
            } else {
                final float radius = getCalculatedCornerRadius();
                canvas.drawRoundRect(mDestination, radius, radius, mBorderPaint);
            }
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (OUTLINE_DRAW) {
            super.setColorFilter(cf);
            return;
        }
        mBitmapPaint.setColorFilter(cf);
    }

	@Override
	 protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		updateShadowBitmap();
	}

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void setBackground(Drawable background) {
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updateShadowBitmap();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        super.setPaddingRelative(start, top, end, bottom);
        updateShadowBitmap();
    }

    private float getCornerRadius() {
        return mCornerRadius;
	}

    public void setCornerRadius(float radius) {
        mCornerRadius = radius;
    }

	private void initOutlineProvider() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setClipToOutline(true);
			setOutlineProvider(new CircularOutlineProvider());
		}
	}

	private void updateShadowBitmap() {
		if (USE_OUTLINE) return;
		final int width = getWidth(), height = getHeight();
		if (width <= 0 || height <= 0) return;
		final int contentLeft = getPaddingLeft(), contentTop = getPaddingTop(),
				contentRight = width - getPaddingRight(),
				contentBottom = height - getPaddingBottom();
		final int contentWidth = contentRight - contentLeft,
				contentHeight = contentBottom - contentTop;
		final float radius = mShadowRadius, dy = radius * 1.5f / 2;
		final int size = Math.round(Math.min(contentWidth, contentHeight) + radius * 2);
		mShadowBitmap = Bitmap.createBitmap(size, Math.round(size + dy), Config.ARGB_8888);
		Canvas canvas = new Canvas(mShadowBitmap);
		final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.WHITE);
		paint.setShadowLayer(radius, 0, radius * 1.5f / 2, SHADOW_START_COLOR);
		final RectF rect = new RectF(radius, radius, size - radius, size - radius);
		canvas.drawOval(rect, paint);
		paint.setShadowLayer(0, 0, 0, 0);
		paint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		canvas.drawOval(rect, paint);
		invalidate();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static class CircularOutlineProvider extends ViewOutlineProvider {
		@Override
		public void getOutline(View view, Outline outline) {
			final int contentLeft = view.getPaddingLeft(), contentTop = view.getPaddingTop(),
					contentRight = view.getWidth() - view.getPaddingRight(),
					contentBottom = view.getHeight() - view.getPaddingBottom();
            final ShapedImageView imageView = (ShapedImageView) view;
            if (imageView.getStyle() == SHAPE_CIRCLE) {
                final int contentWidth = contentRight - contentLeft,
                        contentHeight = contentBottom - contentTop;
                final int size = Math.min(contentWidth, contentHeight);
                outline.setOval(contentLeft + (contentWidth - size) / 2,
                        contentTop + (contentHeight - size) / 2,
                        contentRight - (contentWidth - size) / 2,
                        contentBottom - (contentHeight - size) / 2);
            } else {
                final float radius = imageView.getCalculatedCornerRadius();
                outline.setRoundRect(contentLeft, contentTop, contentRight, contentBottom, radius);
            }
		}
	}

	private float getCalculatedCornerRadius() {
		if (mCornerRadiusRatio > 0) {
			return Math.min(getWidth(), getHeight()) * mCornerRadiusRatio;
		} else if (mCornerRadius > 0) {
			return mCornerRadius;
		}
		return 0;
	}
}
