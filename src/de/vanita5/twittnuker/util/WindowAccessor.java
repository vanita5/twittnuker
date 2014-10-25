package de.vanita5.twittnuker.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Window;

public class WindowAccessor {
	public static void setStatusBarColor(Window window, int color) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
		WindowAccessorL.setStatusBarColor(window, color);
	}

	@TargetApi(Build.VERSION_CODES.L)
	private static class WindowAccessorL {
		public static void setStatusBarColor(Window window, int color) {
			window.setStatusBarColor(color);
		}
	}
}