/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.nostra13.universalimageloader.utils.IoUtils;

import de.vanita5.twittnuker.TwittnukerConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class BitmapUtils {

	// Find the max x that 1 / x <= scale.
	public static int computeSampleSize(final float scale) {
		if (scale <= 0) return 1;
		final int initialSize = Math.max(1, (int) Math.ceil(1 / scale));
		return initialSize <= 8 ? TwidereMathUtils.nextPowerOf2(initialSize) : (initialSize + 7) / 8 * 8;
	}

	// This computes a sample size which makes the longer side at least
	// minSideLength long. If that's not possible, return 1.
	public static int computeSampleSizeLarger(final int w, final int h, final int minSideLength) {
		final int initialSize = Math.max(w / minSideLength, h / minSideLength);
		if (initialSize <= 1) return 1;

		return initialSize <= 8 ? TwidereMathUtils.prevPowerOf2(initialSize) : initialSize / 8 * 8;
	}

    public static boolean downscaleImageIfNeeded(final File imageFile, final int quality) {
        if (imageFile == null || !imageFile.isFile()) return false;
        final String path = imageFile.getAbsolutePath();
        final BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, o);
        // Corrupted image, so return now.
        if (o.outWidth <= 0 || o.outHeight <= 0) return false;
        // Ignore for GIF image
        if ("image/gif".equals(o.outMimeType)) return true;
        o.inJustDecodeBounds = false;
        if (o.outWidth > TwittnukerConstants.TWITTER_MAX_IMAGE_WIDTH || o.outHeight > TwittnukerConstants.TWITTER_MAX_IMAGE_HEIGHT) {
            // The image dimension is larger than Twitter's limit.
            o.inSampleSize = Utils.calculateInSampleSize(o.outWidth, o.outHeight, TwittnukerConstants.TWITTER_MAX_IMAGE_WIDTH,
					TwittnukerConstants.TWITTER_MAX_IMAGE_HEIGHT);
            FileOutputStream fos = null;
            try {
                final Bitmap b = BitmapDecodeHelper.decode(path, o);
                final Bitmap.CompressFormat format = Utils.getBitmapCompressFormatByMimeType(o.outMimeType,
                        Bitmap.CompressFormat.PNG);
                fos = new FileOutputStream(imageFile);
                return b.compress(format, quality, fos);
            } catch (final OutOfMemoryError e) {
                return false;
            } catch (final FileNotFoundException e) {
                // This shouldn't happen.
            } catch (final IllegalArgumentException e) {
                return false;
            } finally {
                IoUtils.closeSilently(fos);
            }
        } else if (imageFile.length() > TwittnukerConstants.TWITTER_MAX_IMAGE_SIZE) {
            // The file size is larger than Twitter's limit.
            FileOutputStream fos = null;
            try {
                final Bitmap b = BitmapDecodeHelper.decode(path, o);
                fos = new FileOutputStream(imageFile);
                return b.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            } catch (final OutOfMemoryError e) {
                return false;
            } catch (final FileNotFoundException e) {
                // This shouldn't happen.
            } finally {
                IoUtils.closeSilently(fos);
            }
        }
        return true;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
				Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

	// Resize the bitmap if each side is >= targetSize * 2
	public static Bitmap resizeDownIfTooBig(final Bitmap bitmap, final int targetSize, final boolean recycle) {
		final int srcWidth = bitmap.getWidth();
		final int srcHeight = bitmap.getHeight();
		final float scale = Math.max((float) targetSize / srcWidth, (float) targetSize / srcHeight);
		if (scale > 0.5f) return bitmap;
		return resizeBitmapByScale(bitmap, scale, recycle);
	}

	private static Bitmap.Config getConfig(final Bitmap bitmap) {
		Bitmap.Config config = bitmap.getConfig();
		if (config == null) {
			config = Bitmap.Config.RGB_565;
		}
		return config;
	}

	private static Bitmap resizeBitmapByScale(final Bitmap bitmap, final float scale, final boolean recycle) {
		final int width = Math.round(bitmap.getWidth() * scale);
		final int height = Math.round(bitmap.getHeight() * scale);
		if (width == bitmap.getWidth() && height == bitmap.getHeight()) return bitmap;
		final Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
		final Canvas canvas = new Canvas(target);
		canvas.scale(scale, scale);
		final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		if (recycle) {
			bitmap.recycle();
		}
		return target;
	}
}