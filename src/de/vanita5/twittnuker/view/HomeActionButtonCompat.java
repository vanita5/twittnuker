package de.vanita5.twittnuker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.accessor.ViewAccessor;
import de.vanita5.twittnuker.view.iface.IHomeActionButton;

public class HomeActionButtonCompat extends FrameLayout implements IHomeActionButton {

    private final ImageView mBackgroundView;
	private final ImageView mIconView;
	private final ProgressBar mProgressBar;

	public HomeActionButtonCompat(final Context context) {
		this(context, null);
	}

	public HomeActionButtonCompat(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HomeActionButtonCompat(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
        ViewAccessor.setBackground(this, null);
        inflate(ThemeUtils.getActionBarContext(context), R.layout.action_item_home_actions_compat, this);
        mBackgroundView = (ImageView) findViewById(R.id.background);
		mIconView = (ImageView) findViewById(android.R.id.icon);
		mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
	}

	@Override
    public void setButtonColor(int color) {
        mBackgroundView.setImageDrawable(new MyColorDrawable(color));
			}

    private static class MyColorDrawable extends ColorDrawable {
        public MyColorDrawable(int color) {
            super(color);
        }

        @Override
        public int getIntrinsicHeight() {
            return 16;
        }

        @Override
        public int getIntrinsicWidth() {
            return 16;
		}
    }

    @Override
    public void setIconColor(int color, Mode mode) {
        mIconView.setColorFilter(color, mode);
	}

	public void setIcon(final Bitmap bm) {
		mIconView.setImageBitmap(bm);
	}

	public void setIcon(final Drawable drawable) {
		mIconView.setImageDrawable(drawable);
	}

	public void setIcon(final int resId) {
		mIconView.setImageResource(resId);
	}

	public void setShowProgress(final boolean showProgress) {
		mProgressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
		mIconView.setVisibility(showProgress ? View.GONE : View.VISIBLE);
	}

	public void setTitle(final CharSequence title) {
		setContentDescription(title);
	}

	public void setTitle(final int title) {
		setTitle(getResources().getText(title));
	}

}