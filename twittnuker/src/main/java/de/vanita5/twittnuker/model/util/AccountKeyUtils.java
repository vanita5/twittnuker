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
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.DataStoreUtils;

import java.util.ArrayList;

public class AccountKeyUtils {

    @Nullable
    public static AccountKey findById(Context context, long id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return null;
        try {
            if (cur.moveToFirst()) return AccountKey.valueOf(cur.getString(0));
        } finally {
            cur.close();
        }
        return null;
    }

    @NonNull
    public static AccountKey[] findByIds(Context context, long... id) {
        final String[] projection = {Accounts.ACCOUNT_KEY};
        final Cursor cur = DataStoreUtils.findAccountCursorsById(context, projection, id);
        if (cur == null) return new AccountKey[0];
        try {
            final ArrayList<AccountKey> accountKeys = new ArrayList<>();
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                accountKeys.add(AccountKey.valueOf(cur.getString(0)));
                cur.moveToNext();
            }
            return accountKeys.toArray(new AccountKey[accountKeys.size()]);
        } finally {
            cur.close();
        }
    }
}