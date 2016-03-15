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

import org.mariotaku.sqliteqb.library.ArgsArray;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableAccountCursorIndices;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.TwidereArrayUtils;

import java.util.List;

public class ParcelableAccountUtils {

    public static UserKey[] getAccountKeys(@NonNull ParcelableAccount[] accounts) {
        UserKey[] ids = new UserKey[accounts.length];
        for (int i = 0, j = accounts.length; i < j; i++) {
            ids[i] = accounts[i].account_key;
        }
        return ids;
    }

    @Nullable
    public static ParcelableAccount getAccount(@NonNull final Context context,
                                               @NonNull final UserKey accountKey) {
        final Cursor c = DataStoreUtils.getAccountCursor(context,
                Accounts.COLUMNS_NO_CREDENTIALS, accountKey);
        if (c == null) return null;
        try {
            final ParcelableAccountCursorIndices i = new ParcelableAccountCursorIndices(c);
            if (c.moveToFirst()) {
                return i.newObject(c);
            }
        } finally {
            c.close();
        }
        return null;
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(final Context context, final boolean activatedOnly,
                                                  final boolean officialKeyOnly) {
        final List<ParcelableAccount> list = DataStoreUtils.getAccountsList(context, activatedOnly, officialKeyOnly);
        return list.toArray(new ParcelableAccount[list.size()]);
    }

    public static ParcelableAccount[] getAccounts(@NonNull final Context context) {
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                Accounts.COLUMNS_NO_CREDENTIALS, null, null, null);
        if (cur == null) return new ParcelableAccount[0];
        return getAccounts(cur, new ParcelableAccountCursorIndices(cur));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@NonNull final Context context, @NonNull final UserKey... accountIds) {
        final String where = Expression.in(new Columns.Column(Accounts.ACCOUNT_KEY),
                new ArgsArray(accountIds.length)).getSQL();
        final String[] whereArgs = TwidereArrayUtils.toStringArray(accountIds);
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                Accounts.COLUMNS_NO_CREDENTIALS, where, whereArgs, null);
        if (cur == null) return new ParcelableAccount[0];
        return getAccounts(cur, new ParcelableAccountCursorIndices(cur));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Cursor cursor) {
        if (cursor == null) return new ParcelableAccount[0];
        return getAccounts(cursor, new ParcelableAccountCursorIndices(cursor));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Cursor cursor, @Nullable final ParcelableAccountCursorIndices indices) {
        if (cursor == null || indices == null) return new ParcelableAccount[0];
        try {
            cursor.moveToFirst();
            final ParcelableAccount[] names = new ParcelableAccount[cursor.getCount()];
            while (!cursor.isAfterLast()) {
                names[cursor.getPosition()] = indices.newObject(cursor);
                cursor.moveToNext();
            }
            return names;
        } finally {
            cursor.close();
        }
    }

    @NonNull
    @ParcelableAccount.Type
    public static String getAccountType(@NonNull ParcelableAccount account) {
        if (account.account_type == null) return ParcelableAccount.Type.TWITTER;
        return account.account_type;
    }
}