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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.annotation.AccountType;
import de.vanita5.twittnuker.library.MicroBlog;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.User;
import de.vanita5.twittnuker.model.AccountDetails;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.message.FriendshipTaskEvent;
import de.vanita5.twittnuker.util.Utils;

public class CreateFriendshipTask extends AbsFriendshipOperationTask implements Constants {

    public CreateFriendshipTask(final Context context) {
        super(context, FriendshipTaskEvent.Action.FOLLOW);
    }

    @NonNull
    @Override
    protected User perform(@NonNull MicroBlog twitter, @NonNull AccountDetails details, @NonNull Arguments args) throws MicroBlogException {
        switch (details.type) {
            case AccountType.FANFOU: {
                return twitter.createFanfouFriendship(args.userKey.getId());
            }
        }
        return twitter.createFriendship(args.userKey.getId());
    }

    @Override
    protected void succeededWorker(@NonNull MicroBlog twitter, @NonNull AccountDetails details, @NonNull Arguments args, @NonNull ParcelableUser user) {
        user.is_following = true;
        Utils.setLastSeen(context, user.key, System.currentTimeMillis());
    }

    @Override
    protected void showErrorMessage(@NonNull Arguments params, @Nullable Exception exception) {
        if (USER_TYPE_FANFOU_COM.equals(params.accountKey.getHost())) {
            // Fanfou returns 403 for follow request
            if (exception instanceof MicroBlogException) {
                MicroBlogException te = (MicroBlogException) exception;
                if (te.getStatusCode() == 403 && !TextUtils.isEmpty(te.getErrorMessage())) {
                    Utils.showErrorMessage(context, te.getErrorMessage(), false);
                    return;
                }
            }
        }
        Utils.showErrorMessage(context, R.string.action_following, exception, false);
    }

    @Override
    protected void showSucceededMessage(@NonNull Arguments params, @NonNull ParcelableUser user) {
        final String message;
        final boolean nameFirst = preferences.getBoolean(KEY_NAME_FIRST);
        if (user.is_protected) {
            message = context.getString(R.string.sent_follow_request_to_user,
                    manager.getDisplayName(user, nameFirst));
        } else {
            message = context.getString(R.string.followed_user,
                    manager.getDisplayName(user, nameFirst));
        }
        Utils.showOkMessage(context, message, false);
    }

}