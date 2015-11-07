/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.AttributeSet;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.constant.SharedPreferenceConstants;

public class PushNotificationStatusPreference extends Preference implements TwittnukerConstants {

    public PushNotificationStatusPreference(final Context context) {
        this(context, null);
    }

    public PushNotificationStatusPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public PushNotificationStatusPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        setTitle(R.string.push_status_title);

        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean(SharedPreferenceConstants.GCM_TOKEN_SENT, false)) {
            setSummary(R.string.push_status_connected);
        } else {
            setSummary(R.string.push_status_disconnected);
        }
    }
}
