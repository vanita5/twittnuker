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
import android.os.Bundle;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.ThemeUtils;

@SuppressLint("Registered")
public class BaseSupportDialogActivity extends ThemedFragmentActivity implements Constants {

	private boolean mInstanceStateSaved;

	@Override
	public int getThemeColor() {
		return ThemeUtils.getThemeColor(this, getThemeResourceId());
	}

	@Override
	public int getActionBarColor() {
		return ThemeUtils.getActionBarColor(this);
	}

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getDialogThemeResource(this);
	}

    public TwittnukerApplication getTwittnukerApplication() {
        return (TwittnukerApplication) getApplication();
    }

	protected boolean isStateSaved() {
		return mInstanceStateSaved;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mInstanceStateSaved = false;
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mInstanceStateSaved = true;
		super.onSaveInstanceState(outState);
	}
}