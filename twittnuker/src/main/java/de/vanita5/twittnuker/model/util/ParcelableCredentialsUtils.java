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

import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableCredentialsCursorIndices;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.DataStoreUtils;

import java.io.IOException;

public class ParcelableCredentialsUtils {
    private ParcelableCredentialsUtils() {
    }

    public static boolean isOAuth(int authType) {
        switch (authType) {
            case ParcelableCredentials.AuthType.OAUTH:
            case ParcelableCredentials.AuthType.XAUTH: {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ParcelableCredentials getCredentials(@NonNull final Context context,
                                                       @NonNull final UserKey accountKey) {
        final Cursor c = DataStoreUtils.getAccountCursor(context, Accounts.COLUMNS, accountKey);
        if (c == null) return null;
        try {
            final ParcelableCredentialsCursorIndices i = new ParcelableCredentialsCursorIndices(c);
            if (c.moveToFirst()) {
                return i.newObject(c);
            }
        } catch (IOException e) {
            return null;
        } finally {
            c.close();
        }
        return null;
    }


    @NonNull
    public static ParcelableCredentials[] getCredentialses(@Nullable final Cursor cursor, @Nullable final ParcelableCredentialsCursorIndices indices) {
        if (cursor == null || indices == null) return new ParcelableCredentials[0];
        try {
            cursor.moveToFirst();
            final ParcelableCredentials[] credentialses = new ParcelableCredentials[cursor.getCount()];
            while (!cursor.isAfterLast()) {
                credentialses[cursor.getPosition()] = indices.newObject(cursor);
                cursor.moveToNext();
            }
            return credentialses;
        }  catch (IOException e) {
            return new ParcelableCredentials[0];
        } finally {
            cursor.close();
        }
    }


    public static ParcelableCredentials[] getCredentialses(@NonNull final Context context) {
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                Accounts.COLUMNS, null, null, null);
        if (cur == null) return new ParcelableCredentials[0];
        return getCredentialses(cur, new ParcelableCredentialsCursorIndices(cur));
    }
}