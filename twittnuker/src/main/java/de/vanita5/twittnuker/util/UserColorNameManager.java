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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.util.UserKeyUtils;

import java.util.Map;
import java.util.Set;

import static android.text.TextUtils.isEmpty;

public class UserColorNameManager implements TwittnukerConstants {

    private final SharedPreferences mColorPreferences;

    public UserColorNameManager(Context context) {
        mColorPreferences = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static String decideDisplayName(final String name, final String screenName, final boolean nameFirst) {
        return nameFirst && !isEmpty(name) ? name : "@" + screenName;
    }

    public void registerColorChangedListener(final UserColorChangedListener listener) {

        mColorPreferences.registerOnSharedPreferenceChangeListener(new OnColorPreferenceChangeListener(listener));
    }

    public void clearUserColor(@NonNull final UserKey userId) {
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        editor.remove(userId.toString());
        editor.apply();
    }

    public void setUserColor(@NonNull final UserKey userKey, final int color) {
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        editor.putInt(userKey.toString(), color);
        editor.apply();
    }

    public String getDisplayName(final ParcelableUser user, final boolean nameFirst) {
        return getDisplayName(user.key, user.name, user.screen_name, nameFirst);
    }

    public String getDisplayName(final User user, final boolean nameFirst) {
        return getDisplayName(UserKeyUtils.fromUser(user), user.getName(), user.getScreenName(), nameFirst);
    }

    public String getDisplayName(final ParcelableUserList user, final boolean nameFirst) {
        return getDisplayName(user.user_key, user.user_name, user.user_screen_name, nameFirst);
    }

    public String getDisplayName(final ParcelableStatus status, final boolean nameFirst) {
        return getDisplayName(status.user_key, status.user_name, status.user_screen_name, nameFirst);
    }

    public String getDisplayName(@NonNull final UserKey userId, final String name,
                                 final String screenName, final boolean nameFirst) {
        return getDisplayName(userId.toString(), name, screenName, nameFirst);
    }

    public String getDisplayName(@NonNull final String userId, final String name,
                                 final String screenName, final boolean nameFirst) {
        return decideDisplayName(name, screenName, nameFirst);
    }

    public int getUserColor(@NonNull final UserKey userId) {
        return getUserColor(userId.toString());
    }

    public int getUserColor(@NonNull final String userId) {
        return mColorPreferences.getInt(userId, Color.TRANSPARENT);
    }

    public String getName(@NonNull final UserKey id, String name) {
        return name;
    }

    public interface UserColorChangedListener {
        void onUserColorChanged(@NonNull UserKey userId, int color);
    }

    private static final class OnColorPreferenceChangeListener implements OnSharedPreferenceChangeListener {

        private final UserColorChangedListener mListener;

        OnColorPreferenceChangeListener(final UserColorChangedListener listener) {
            mListener = listener;
        }

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
            final UserKey userId = UserKey.valueOf(key);
            if (mListener != null && userId != null) {
                mListener.onUserColorChanged(userId, sharedPreferences.getInt(key, 0));
            }
        }

    }
}