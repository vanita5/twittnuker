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
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.message.FriendshipTaskEvent;
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.util.Utils;

public class DestroyFriendshipTask extends AbsFriendshipOperationTask {

    public DestroyFriendshipTask(final Context context) {
        super(context, FriendshipTaskEvent.Action.UNFOLLOW);
    }

    @NonNull
    @Override
    protected User perform(@NonNull Twitter twitter, @NonNull ParcelableCredentials credentials, @NonNull Arguments args) throws TwitterException {
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.FANFOU: {
                return twitter.destroyFanfouFriendship(args.userKey.getId());
            }
        }
        return twitter.destroyFriendship(args.userKey.getId());
    }

    @Override
    protected void succeededWorker(@NonNull Twitter twitter, @NonNull ParcelableCredentials credentials, @NonNull Arguments args, @NonNull ParcelableUser user) {
        user.is_following = false;
        Utils.setLastSeen(context, user.key, -1);
        final Expression where = Expression.and(Expression.equalsArgs(TwidereDataStore.Statuses.ACCOUNT_KEY),
                Expression.or(Expression.equalsArgs(TwidereDataStore.Statuses.USER_KEY),
                        Expression.equalsArgs(TwidereDataStore.Statuses.RETWEETED_BY_USER_KEY)));
        final String[] whereArgs = {args.userKey.toString(), args.userKey.toString(),
                args.userKey.toString()};
        final ContentResolver resolver = context.getContentResolver();
        resolver.delete(TwidereDataStore.Statuses.CONTENT_URI, where.getSQL(), whereArgs);
    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        Utils.showErrorMessage(context, R.string.action_unfollowing, exception, false);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final boolean nameFirst = preferences.getBoolean(KEY_NAME_FIRST);
        final String message = context.getString(R.string.unfollowed_user,
                manager.getDisplayName(user, nameFirst));
        Utils.showInfoMessage(context, message, false);
    }

}