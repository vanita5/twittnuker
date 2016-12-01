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

package de.vanita5.twittnuker.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.Tab;
import de.vanita5.twittnuker.model.TabCursorIndices;
import de.vanita5.twittnuker.model.TabValuesCreator;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.tab.argument.TabArguments;
import de.vanita5.twittnuker.provider.TwidereDataStore.AccountSupportColumns;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedRelationships;
import de.vanita5.twittnuker.provider.TwidereDataStore.DirectMessages;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.provider.TwidereDataStore.Tabs;
import de.vanita5.twittnuker.util.JsonSerializer;

import java.io.IOException;

import kotlin.Pair;

public class UpdateAccountInfoTask extends AbstractTask<Pair<ParcelableAccount, ParcelableUser>, Object, Object> {
    private final Context context;

    public UpdateAccountInfoTask(Context context) {
        this.context = context;
    }

    @Override
    protected Object doLongOperation(Pair<ParcelableAccount, ParcelableUser> params) {
        final ContentResolver resolver = context.getContentResolver();
        final ParcelableAccount account = params.getFirst();
        final ParcelableUser user = params.getSecond();
        if (account == null || user == null) return null;
        if (user.is_cache) {
            return null;
        }
        if (!user.key.maybeEquals(user.account_key)) {
            return null;
        }

        final String accountWhere = Expression.equalsArgs(Accounts._ID).getSQL();
        final String[] accountWhereArgs = {String.valueOf(account.id)};

        final ContentValues accountValues = new ContentValues();
        accountValues.put(Accounts.NAME, user.name);
        accountValues.put(Accounts.SCREEN_NAME, user.screen_name);
        accountValues.put(Accounts.PROFILE_IMAGE_URL, user.profile_image_url);
        accountValues.put(Accounts.PROFILE_BANNER_URL, user.profile_banner_url);
        accountValues.put(Accounts.ACCOUNT_USER, JsonSerializer.serialize(user,
                ParcelableUser.class));
        accountValues.put(Accounts.ACCOUNT_KEY, String.valueOf(user.key));

        resolver.update(Accounts.CONTENT_URI, accountValues, accountWhere, accountWhereArgs);

        final ContentValues accountKeyValues = new ContentValues();
        accountKeyValues.put(AccountSupportColumns.ACCOUNT_KEY, String.valueOf(user.key));
        final String accountKeyWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).getSQL();
        final String[] accountKeyWhereArgs = {account.account_key.toString()};


        resolver.update(Statuses.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);
        resolver.update(DirectMessages.Inbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);
        resolver.update(DirectMessages.Outbox.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountKeyWhere, accountKeyWhereArgs);

        updateTabs(context, resolver, user.key);


        return null;
    }

    private void updateTabs(@NonNull Context context, @NonNull ContentResolver resolver, UserKey accountKey) {
        Cursor tabsCursor = resolver.query(Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, null);
        if (tabsCursor == null) return;
        try {
            TabCursorIndices indices = new TabCursorIndices(tabsCursor);
            tabsCursor.moveToFirst();
            LongSparseArray<ContentValues> values = new LongSparseArray<>();
            while (!tabsCursor.isAfterLast()) {
                Tab tab = indices.newObject(tabsCursor);
                TabArguments arguments = tab.getArguments();
                if (arguments != null) {
                    final String accountId = arguments.getAccountId();
                    final UserKey[] keys = arguments.getAccountKeys();
                    if (TextUtils.equals(accountKey.getId(), accountId) && keys == null) {
                        arguments.setAccountKeys(new UserKey[]{accountKey});
                        values.put(tab.getId(), TabValuesCreator.create(tab));
                    }
                }
                tabsCursor.moveToNext();
            }
            final String where = Expression.equalsArgs(Tabs._ID).getSQL();
            for (int i = 0, j = values.size(); i < j; i++) {
                final String[] whereArgs = {String.valueOf(values.keyAt(i))};
                resolver.update(Tabs.CONTENT_URI, values.valueAt(i), where, whereArgs);
            }
        } catch (IOException e) {
            // Ignore
        } finally {
            tabsCursor.close();
        }
    }
}