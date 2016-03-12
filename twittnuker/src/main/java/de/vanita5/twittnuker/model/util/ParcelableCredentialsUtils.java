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

import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableCredentialsCursorIndices;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.DataStoreUtils;

public class ParcelableCredentialsUtils {
    public static boolean isOAuth(int authType) {
        switch (authType) {
            case ParcelableCredentials.AUTH_TYPE_OAUTH:
            case ParcelableCredentials.AUTH_TYPE_XAUTH: {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ParcelableCredentials getCredentials(@NonNull final Context context,
                                                       @NonNull final AccountKey accountKey) {
        final Cursor c = DataStoreUtils.getAccountCursor(context,
                Accounts.COLUMNS, accountKey);
        if (c == null) return null;
        try {
            final ParcelableCredentialsCursorIndices i = new ParcelableCredentialsCursorIndices(c);
            if (c.moveToFirst()) {
                return i.newObject(c);
            }
        } finally {
            c.close();
        }
        return null;
    }
}