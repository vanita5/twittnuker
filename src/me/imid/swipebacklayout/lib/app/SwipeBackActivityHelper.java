package me.imid.swipebacklayout.lib.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.SwipebackActivityUtils.SwipebackScreenshotManager;

/**
 * @author Yrom
 * 
 */
public class SwipeBackActivityHelper implements TwittnukerConstants {
	private final Activity mActivity;
	private SwipeBackLayout mSwipeBackLayout;

	public SwipeBackActivityHelper(final Activity activity) {
		mActivity = activity;
	}

	public View findViewById(final int id) {
		if (mSwipeBackLayout != null) return mSwipeBackLayout.findViewById(id);
		return null;
	}

	public SwipeBackLayout getSwipeBackLayout() {
		return mSwipeBackLayout;
	}

	public void onActivtyCreate() {
		final Window w = mActivity.getWindow();
		w.setBackgroundDrawable(new ColorDrawable(0));
		mSwipeBackLayout = (SwipeBackLayout) LayoutInflater.from(mActivity).inflate(R.layout.swipeback_layout, null);
	}
	
	public void onDestroy() {
		if (mActivity.isFinishing()) {
			final Intent intent = mActivity.getIntent();
			final TwittnukerApplication app = TwittnukerApplication.getInstance(mActivity);
			final SwipebackScreenshotManager sm = app.getSwipebackScreenshotManager();
			sm.remove(intent.getLongExtra(EXTRA_ACTIVITY_SCREENSHOT_ID, -1));
		}
	}

	public void onPostCreate() {
		mSwipeBackLayout.attachToActivity(mActivity);
		final Intent intent = mActivity.getIntent();
		final TwittnukerApplication app = TwittnukerApplication.getInstance(mActivity);
		final SwipebackScreenshotManager sm = app.getSwipebackScreenshotManager();
		final Bitmap b = sm.get(intent.getLongExtra(EXTRA_ACTIVITY_SCREENSHOT_ID, -1));
        if (b != null) {
            mSwipeBackLayout.setWindowBackgroundDrawable(new BitmapDrawable(mActivity.getResources(), b));
        }
        mSwipeBackLayout.setEnableGesture(b != null);
	}

}
