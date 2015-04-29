package de.vanita5.twittnuker.util.support;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Window;

public class WindowSupport {
	public static void setStatusBarColor(Window window, int color) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        WindowAccessorLollipop.setStatusBarColor(window, color);
	}

    public static void setSharedElementsUseOverlay(Window window, boolean sharedElementsUseOverlay) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        WindowAccessorLollipop.setSharedElementsUseOverlay(window, sharedElementsUseOverlay);
    }

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static class WindowAccessorLollipop {
		public static void setStatusBarColor(Window window, int color) {
			window.setStatusBarColor(color);
		}

        public static void setSharedElementsUseOverlay(Window window, boolean sharedElementsUseOverlay) {
            window.setSharedElementsUseOverlay(sharedElementsUseOverlay);
        }
	}
}