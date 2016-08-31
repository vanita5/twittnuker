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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import de.vanita5.twittnuker.library.twitter.model.User;
import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.util.UserKeyUtils;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;

import static android.text.TextUtils.isEmpty;

public class UserColorNameManager implements TwittnukerConstants {

    private final SharedPreferences mColorPreferences;
    private final Context mContext;

    public UserColorNameManager(Context context) {
        mContext = context;
        mColorPreferences = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static String decideDisplayName(final String name, final String screenName, final boolean nameFirst) {
        return nameFirst && !isEmpty(name) ? name : "@" + screenName;
    }

    public void registerColorChangedListener(final UserColorChangedListener listener) {

        mColorPreferences.registerOnSharedPreferenceChangeListener(new OnColorPreferenceChangeListener(listener));
    }

    public void clearUserColor(@NonNull final UserKey userKey) {
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        final String userKeyString = userKey.toString();
        updateColor(userKeyString, 0);
        editor.remove(userKeyString);
        editor.apply();
    }

    public void setUserColor(@NonNull final UserKey userKey, final int color) {
        final SharedPreferences.Editor editor = mColorPreferences.edit();
        final String userKeyString = userKey.toString();
        updateColor(userKeyString, color);
        editor.putInt(userKeyString, color);
        editor.apply();
    }

    private void updateColor(String userKey, int color) {
        final ContentResolver cr = mContext.getContentResolver();
        ContentValues cv = new ContentValues();
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.USER_COLOR, Statuses.USER_KEY,
                color, cv);
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.QUOTED_USER_COLOR,
                Statuses.QUOTED_USER_KEY, color, cv);
        updateColumn(cr, Statuses.CONTENT_URI, userKey, Statuses.RETWEET_USER_COLOR,
                Statuses.RETWEETED_BY_USER_KEY, color, cv);

        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_USER_COLOR,
                Activities.STATUS_USER_KEY, color, cv);
        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_RETWEET_USER_COLOR,
                Activities.STATUS_RETWEETED_BY_USER_KEY, color, cv);
        updateColumn(cr, Activities.AboutMe.CONTENT_URI, userKey, Activities.STATUS_QUOTED_USER_COLOR,
                Activities.STATUS_QUOTED_USER_KEY, color, cv);
    }

    private static void updateColumn(ContentResolver cr, Uri uri, String userKey, String valueColumn,
                                     String whereColumn, int value, ContentValues temp) {
        temp.clear();
        temp.put(valueColumn, value);
        cr.update(uri, temp, Expression.equalsArgs(whereColumn).getSQL(),
                new String[]{userKey});
    }


    private static void updateColumn(ContentResolver cr, Uri uri, String userKey, String valueColumn,
                                     String whereColumn, String value, ContentValues temp) {
        temp.clear();
        temp.put(valueColumn, value);
        cr.update(uri, temp, Expression.equalsArgs(whereColumn).getSQL(),
                new String[]{userKey});
    }

    @WorkerThread
    public String getDisplayName(final ParcelableUser user, final boolean nameFirst) {
        return getDisplayName(user.key, user.name, user.screen_name, nameFirst);
    }

    @WorkerThread
    public String getDisplayName(final User user, final boolean nameFirst) {
        return getDisplayName(UserKeyUtils.fromUser(user), user.getName(), user.getScreenName(), nameFirst);
    }

    @WorkerThread
    public String getDisplayName(final ParcelableUserList user, final boolean nameFirst) {
        return getDisplayName(user.user_key, user.user_name, user.user_screen_name, nameFirst);
    }

    @WorkerThread
    public String getDisplayName(final ParcelableStatus status, final boolean nameFirst) {
        return getDisplayName(status.user_key, status.user_name, status.user_screen_name, nameFirst);
    }

    @WorkerThread
    public String getDisplayName(@NonNull final UserKey userId, final String name,
                                 final String screenName, final boolean nameFirst) {
        return getDisplayName(userId.toString(), name, screenName, nameFirst);
    }

    @WorkerThread
    public String getDisplayName(@NonNull final String userId, final String name,
                                 final String screenName, final boolean nameFirst) {
        return decideDisplayName(name, screenName, nameFirst);
    }

    @WorkerThread
    public int getUserColor(@NonNull final UserKey userId) {
        return getUserColor(userId.toString());
    }

    @WorkerThread
    public int getUserColor(@NonNull final String userId) {
        return mColorPreferences.getInt(userId, Color.TRANSPARENT);
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
        public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
            final UserKey userId = UserKey.valueOf(key);
            if (mListener != null && userId != null) {
                mListener.onUserColorChanged(userId, preferences.getInt(key, 0));
            }
        }

    }
}