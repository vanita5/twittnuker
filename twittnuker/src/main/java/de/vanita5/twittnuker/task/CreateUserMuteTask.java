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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.message.FriendshipTaskEvent;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedRelationships;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.util.Utils;

public class CreateUserMuteTask extends AbsFriendshipOperationTask {
    public CreateUserMuteTask(Context context) {
        super(context, FriendshipTaskEvent.Action.MUTE);
    }

    @NonNull
    @Override
    protected User perform(@NonNull Twitter twitter, @NonNull ParcelableCredentials credentials,
                           @NonNull Arguments args) throws TwitterException {
        return twitter.createMute(args.userKey.getId());
    }

    @Override
    protected void succeededWorker(@NonNull Twitter twitter,
                                   @NonNull ParcelableCredentials credentials,
                                   @NonNull Arguments args, @NonNull ParcelableUser user) {
        final ContentResolver resolver = context.getContentResolver();
        Utils.setLastSeen(context, args.userKey, -1);
        for (final Uri uri : TwidereDataStore.STATUSES_URIS) {
            final Expression where = Expression.and(
                    Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                    Expression.equalsArgs(Statuses.USER_KEY)
            );
            final String[] whereArgs = {args.accountKey.toString(), args.userKey.toString()};
            resolver.delete(uri, where.getSQL(), whereArgs);
        }
        if (!user.is_following) {
            for (final Uri uri : TwidereDataStore.ACTIVITIES_URIS) {
                final Expression where = Expression.and(
                        Expression.equalsArgs(Activities.ACCOUNT_KEY),
                        Expression.equalsArgs(Activities.STATUS_USER_KEY)
                );
                final String[] whereArgs = {args.accountKey.toString(), args.userKey.toString()};
                resolver.delete(uri, where.getSQL(), whereArgs);
            }
        }
        // I bet you don't want to see this user in your auto complete list.
        final ContentValues values = new ContentValues();
        values.put(CachedRelationships.ACCOUNT_KEY, args.accountKey.toString());
        values.put(CachedRelationships.USER_KEY, args.userKey.toString());
        values.put(CachedRelationships.MUTING, true);
        resolver.insert(CachedRelationships.CONTENT_URI, values);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final boolean nameFirst = preferences.getBoolean(KEY_NAME_FIRST);
        final String message = context.getString(R.string.muted_user, manager.getDisplayName(user,
                nameFirst));
        Utils.showInfoMessage(context, message, false);

    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        Utils.showErrorMessage(context, R.string.action_muting, exception, true);
    }
}