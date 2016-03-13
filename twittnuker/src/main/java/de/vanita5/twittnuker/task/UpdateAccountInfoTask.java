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
import android.util.Pair;

import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.provider.TwidereDataStore.AccountSupportColumns;
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedRelationships;
import de.vanita5.twittnuker.provider.TwidereDataStore.DirectMessages;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.util.JsonSerializer;
import de.vanita5.twittnuker.util.Utils;

public class UpdateAccountInfoTask extends AbstractTask<Pair<UserKey, ParcelableUser>, Object, Object> {
    private final Context context;

    public UpdateAccountInfoTask(Context context) {
        this.context = context;
    }

    @Override
    protected Object doLongOperation(Pair<UserKey, ParcelableUser> params) {
        final ContentResolver resolver = context.getContentResolver();
        final UserKey accountKey = params.first;
        final ParcelableUser user = params.second;
        if (!Utils.isMyAccount(context, user.key)) {
            return null;
        }

        final String accountWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY).getSQL();
        final String[] accountWhereArgs = {accountKey.toString()};

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

        resolver.update(Statuses.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);
        resolver.update(Activities.AboutMe.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);
        resolver.update(DirectMessages.Inbox.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);
        resolver.update(DirectMessages.Outbox.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);
        resolver.update(CachedRelationships.CONTENT_URI, accountKeyValues, accountWhere, accountWhereArgs);

        return null;
    }
}