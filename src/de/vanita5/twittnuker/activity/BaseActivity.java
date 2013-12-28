/*
 *			Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.fragment.support.BasePullToRefreshListFragment;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.MessagesManager;
import de.vanita5.twittnuker.util.ThemeUtils;

@SuppressLint("Registered")
public class BaseActivity extends BaseThemedActivity implements Constants {

	private boolean mInstanceStateSaved, mIsVisible, mIsOnTop;

    public MessagesManager getMessagesManager() {
        return getTwidereApplication() != null ? getTwidereApplication().getMessagesManager() : null;
    }

	@Override
    public int getThemeColor() {
        return ThemeUtils.getUserThemeColor(this);
	}

    @Override
    public int getThemeResource() {
        return ThemeUtils.getThemeResource(this);
	}

	public TwittnukerApplication getTwidereApplication() {
		return (TwittnukerApplication) getApplication();
	}

	public AsyncTwitterWrapper getTwitterWrapper() {
		return getTwidereApplication() != null ? getTwidereApplication().getTwitterWrapper() : null;
	}

	public boolean isOnTop() {
		return mIsOnTop;
	}

	public boolean isVisible() {
		return mIsVisible;
	}

	protected BasePullToRefreshListFragment getCurrentPullToRefreshFragment() {
		return null;
	}

	protected boolean isStateSaved() {
		return mInstanceStateSaved;
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
		final MessagesManager croutons = getMessagesManager();
		if (croutons != null) {
			croutons.addMessageCallback(this);
		}
	}

	@Override
	protected void onStop() {
		mIsVisible = false;
		final MessagesManager croutons = getMessagesManager();
		if (croutons != null) {
			croutons.removeMessageCallback(this);
		}
		super.onStop();
	}

}
