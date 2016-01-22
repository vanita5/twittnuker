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

import java.util.List;

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.PageableResponseList;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableUser;

public class UserFriendsLoader extends CursorSupportUsersLoader {

    private final long mUserId;
    private final String mScreenName;

    public UserFriendsLoader(final Context context, final long accountId, final long userId,
                             final String screenName, final long maxId, final List<ParcelableUser> userList,
                             boolean fromUser) {
        super(context, accountId, maxId, userList, fromUser);
        mUserId = userId;
        mScreenName = screenName;
    }

    @NonNull
    @Override
    protected PageableResponseList<User> getCursoredUsers(@NonNull final Twitter twitter, final Paging paging)
            throws TwitterException {
        if (mUserId > 0)
            return twitter.getFriendsList(mUserId, paging);
        else if (mScreenName != null) return twitter.getFriendsList(mScreenName, paging);
        throw new TwitterException("user_id or screen_name required");
    }

}