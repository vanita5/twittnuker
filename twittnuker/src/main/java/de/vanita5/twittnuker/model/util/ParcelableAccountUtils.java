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

import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableAccountCursorIndices;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.util.DataStoreUtils;

import java.util.List;

public class ParcelableAccountUtils {

    public static AccountKey[] getAccountKeys(@NonNull ParcelableAccount[] accounts) {
        AccountKey[] ids = new AccountKey[accounts.length];
        for (int i = 0, j = accounts.length; i < j; i++) {
            ids[i] = new AccountKey(accounts[i].account_key, accounts[i].account_host);
        }
        return ids;
    }

    @Nullable
    public static ParcelableAccount getAccount(final Context context, final long accountId,
                                               final String accountHost) {
        if (context == null || accountId < 0) return null;
        final Expression where = Expression.equals(Accounts.ACCOUNT_KEY, accountId);
        Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI,
                Accounts.COLUMNS_NO_CREDENTIALS, where.getSQL(), null, null);
        if (cur == null) return null;
        try {
            ParcelableAccountCursorIndices i = new ParcelableAccountCursorIndices(cur);
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                if (TextUtils.equals(cur.getString(i.account_host), accountHost)) {
                    return i.newObject(cur);
                }
                cur.moveToNext();
            }
            if (cur.moveToFirst()) {
                return i.newObject(cur);
            }
        } finally {
            cur.close();
        }
        return null;
    }

    public static ParcelableAccount getAccount(final Context context, final AccountKey accountKey) {
        return getAccount(context, accountKey.getId(), accountKey.getHost());
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(final Context context, final boolean activatedOnly,
                                                  final boolean officialKeyOnly) {
        final List<ParcelableAccount> list = DataStoreUtils.getAccountsList(context, activatedOnly, officialKeyOnly);
        return list.toArray(new ParcelableAccount[list.size()]);
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@Nullable final Context context, @Nullable final AccountKey... accountIds) {
        if (context == null) return new ParcelableAccount[0];
        final String where = accountIds != null ? Expression.in(new Columns.Column(Accounts.ACCOUNT_KEY),
                new RawItemArray(AccountKey.getIds(accountIds))).getSQL() : null;
        final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS_NO_CREDENTIALS, where, null, null);
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

}