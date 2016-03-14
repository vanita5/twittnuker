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

package de.vanita5.twittnuker.model.util;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.media.preview.PreviewMediaExtractor;

import java.util.ArrayList;

public class UserKeyUtils {

    @Nullable
    public static UserKey findById(Context context, long id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) return UserKey.valueOf(cur.getString(0));
        } finally {
            cur.close();
        }
        return null;
    }

    @NonNull
    public static UserKey[] findByIds(Context context, long... id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return new UserKey[0];
        try {
            final ArrayList<UserKey> accountKeys = new ArrayList<>();
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                accountKeys.add(UserKey.valueOf(cur.getString(0)));
                cur.moveToNext();
            }
            return accountKeys.toArray(new UserKey[accountKeys.size()]);
        } finally {
            cur.close();
        }
    }

    public static UserKey fromUser(User user) {
        return new UserKey(user.getId(), getUserHost(user));
    }

    public static String getUserHost(User user) {
        return getUserHost(user.getOstatusUri());
    }

    @NonNull
    public static String getUserHost(@Nullable String uri) {
        if (uri == null) return TwittnukerConstants.USER_TYPE_TWITTER_COM;
        final String authority = PreviewMediaExtractor.getAuthority(uri);
        if (authority == null) return TwittnukerConstants.USER_TYPE_TWITTER_COM;
        return authority.replaceAll("[^\\w\\d\\.]", "-");
    }

    public static boolean isSameHost(UserKey accountKey, UserKey userKey) {
        return isSameHost(accountKey.getHost(), userKey.getHost());
    }

    public static boolean isSameHost(String a, String b) {
        if (TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) return true;
        return TextUtils.equals(a, b);
    }
}