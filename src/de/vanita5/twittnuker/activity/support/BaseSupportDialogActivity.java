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

package de.vanita5.twittnuker.activity.support;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.iface.IThemedActivity;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.ThemeUtils;


@SuppressLint("Registered")
public class BaseSupportDialogActivity extends BaseSupportThemedActivity implements Constants, IThemedActivity {

	private boolean mInstanceStateSaved;

	@Override
    public Resources getResources() {
        return getDefaultResources();
	}

	@Override
	public int getThemeColor() {
		return ThemeUtils.getThemeColor(this);
	}

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getDialogThemeResource(this);
	}

    public TwittnukerApplication getTwittnukerApplication() {
        return (TwittnukerApplication) getApplication();
    }

    @Override
    public final boolean shouldOverrideActivityAnimation() {
        // Dialog theme should never use custom animations
        return false;
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
