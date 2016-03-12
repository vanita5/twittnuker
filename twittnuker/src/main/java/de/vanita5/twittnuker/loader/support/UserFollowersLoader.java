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

package de.vanita5.twittnuker.loader.support;

import android.content.Context;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.util.DataStoreUtils;

import java.util.List;

public class UserFollowersLoader extends CursorSupportUsersLoader {

    private final long mUserId;
    private final String mScreenName;

    public UserFollowersLoader(final Context context, final AccountKey accountId, final long userId,
                               final String screenName, final List<ParcelableUser> data,
                               final boolean fromUser) {
        super(context, accountId, data, fromUser);
        mUserId = userId;
        mScreenName = screenName;
    }

    @NonNull
    @Override
    protected ResponseList<User> getCursoredUsers(@NonNull final Twitter twitter, final Paging paging)
            throws TwitterException {
        final AccountKey accountId = getAccountId();
        if (accountId == null) throw new TwitterException("No account");
        final String accountType = DataStoreUtils.getAccountType(getContext(), accountId);
        if (mUserId > 0) {
            if (ParcelableCredentials.ACCOUNT_TYPE_STATUSNET.equals(accountType)) {
                return twitter.getStatusesFollowersList(mUserId, paging);
            }
            return twitter.getFollowersList(mUserId, paging);
        } else if (mScreenName != null) {
            if (ParcelableCredentials.ACCOUNT_TYPE_STATUSNET.equals(accountType)) {
                return twitter.getStatusesFollowersList(mScreenName, paging);
            }
            return twitter.getFollowersList(mScreenName, paging);
        }
        throw new TwitterException("user_id or screen_name required");
    }

}