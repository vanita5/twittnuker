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

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Debug;
import android.os.StatFs;

import java.io.File;

/**
 * Class containing some static utility methods.
 */
public class ImageLoaderUtils {

	/**
	 * Get the size in bytes of a bitmap.
	 * 
	 * @param bitmap
	 * @return size in bytes
	 */
	public static int getBitmapSize(final Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
			return GetBitmapSizeAccessor.getBitmapSize(bitmap);
		// Pre HC-MR1
		return bitmap.getRowBytes() * bitmap.getHeight();
	}

	/**
	 * Get the memory class of this device (approx. per-app memory limit)
	 * 
	 * @param context
	 * @return
	 */
	public static int getMemoryClass(final Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) return GetMemoryClassAccessor.getMemoryClass(context);
		return (int) (Debug.getNativeHeapSize() / 1024 / 1024);
	}

	/**
	 * Check how much usable space is available at a given path.
	 * 
	 * @param path The path to check
	 * @return The space available in bytes
	 */
	@SuppressWarnings("deprecation")
	public static long getUsableSpace(final File path) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
			return GetUsableSpaceAccessor.getUsableSpace(path);
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}

	/**
	 * Check if OS version has a http URLConnection bug. See here for more
	 * information:
	 * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
	 * 
	 * @return
	 */
	public static boolean hasHttpConnectionBug() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
	}

	static class GetBitmapSizeAccessor {

		@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
		static int getBitmapSize(final Bitmap bitmap) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) return bitmap.getByteCount();
			// Pre HC-MR1
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
	}

	static class GetMemoryClassAccessor {

		@TargetApi(Build.VERSION_CODES.ECLAIR)
		public static int getMemoryClass(final Context context) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
				return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
			return (int) (Debug.getNativeHeapSize() / 1024 / 1024);
		}
	}

	static class GetUsableSpaceAccessor {

		@SuppressWarnings("deprecation")
		@TargetApi(Build.VERSION_CODES.GINGERBREAD)
		public static long getUsableSpace(final File path) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) return path.getUsableSpace();
			final StatFs stats = new StatFs(path.getPath());
			return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
		}
	}
}
