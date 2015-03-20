/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.activity.support;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MenuItem;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.iface.IControlBarActivity;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import de.vanita5.twittnuker.fragment.iface.IBasePullToRefreshFragment;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MessagesManager;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.view.iface.IExtendedView.OnFitSystemWindowsListener;

import java.util.ArrayList;

@SuppressLint("Registered")
public class BaseActionBarActivity extends ThemedActionBarActivity implements Constants,
        OnFitSystemWindowsListener, SystemWindowsInsetsCallback, IControlBarActivity {

	private boolean mInstanceStateSaved, mIsVisible, mIsOnTop;

    private Rect mSystemWindowsInsets;
    private ArrayList<ControlBarOffsetListener> mControlBarOffsetListeners = new ArrayList<>();

	public MessagesManager getMessagesManager() {
		return getTwittnukerApplication() != null ? getTwittnukerApplication().getMessagesManager() : null;
	}

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this, getThemeResourceId());
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getThemeResource(this);
    }

	public TwittnukerApplication getTwittnukerApplication() {
		return (TwittnukerApplication) getApplication();
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		return getTwittnukerApplication() != null ? getTwittnukerApplication().getTwitterWrapper() : null;
	}

	public boolean isOnTop() {
		return mIsOnTop;
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_BACK: {
				onBackPressed();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void startActivity(final Intent intent) {
		super.startActivity(intent);
	}

	@Override
	public void startActivityForResult(final Intent intent, final int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

	protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
		return null;
	}

	protected boolean isStateSaved() {
		return mInstanceStateSaved;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		mIsOnTop = false;
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mInstanceStateSaved = false;
		mIsOnTop = true;
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mInstanceStateSaved = true;
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mIsVisible = true;
		final MessagesManager manager = getMessagesManager();
		if (manager != null) {
			manager.addMessageCallback(this);
		}
	}

	@Override
	protected void onStop() {
		mIsVisible = false;
		final MessagesManager manager = getMessagesManager();
		if (manager != null) {
			manager.removeMessageCallback(this);
		}
		super.onStop();
	}


    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        if (mSystemWindowsInsets == null) return false;
        insets.set(mSystemWindowsInsets);
        return true;
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        mSystemWindowsInsets = new Rect(insets);
    }

    @Override
    public void setControlBarOffset(float offset) {

    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {

    }

    @Override
    public float getControlBarOffset() {
        return 0;
    }

    @Override
    public int getControlBarHeight() {
        return 0;
    }

    @Override
    public void notifyControlBarOffsetChanged() {
        final float offset = getControlBarOffset();
        for (final ControlBarOffsetListener l : mControlBarOffsetListeners) {
            l.onControlBarOffsetChanged(this, offset);
        }
    }

    @Override
    public void registerControlBarOffsetListener(ControlBarOffsetListener listener) {
        mControlBarOffsetListeners.add(listener);
    }

    @Override
    public void unregisterControlBarOffsetListener(ControlBarOffsetListener listener) {
        mControlBarOffsetListeners.remove(listener);
    }
}