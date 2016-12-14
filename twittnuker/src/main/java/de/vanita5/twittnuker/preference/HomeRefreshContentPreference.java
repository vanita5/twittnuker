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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.util.AttributeSet;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;

public class HomeRefreshContentPreference extends MultiSelectListPreference implements Constants {

    public HomeRefreshContentPreference(final Context context) {
        this(context, null);
    }

    public HomeRefreshContentPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public HomeRefreshContentPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected boolean[] getDefaults() {
        return new boolean[]{true, true, false, false};
    }

    @Override
    protected String[] getKeys() {
        return new String[]{KEY_HOME_REFRESH_MENTIONS, KEY_HOME_REFRESH_DIRECT_MESSAGES, KEY_HOME_REFRESH_TRENDS, KEY_HOME_REFRESH_SAVED_SEARCHES};
    }

    @Override
    protected String[] getNames() {
        return getContext().getResources().getStringArray(R.array.entries_home_refresh_content);
    }

}