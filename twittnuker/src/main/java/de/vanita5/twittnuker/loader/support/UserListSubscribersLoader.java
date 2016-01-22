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

public class UserListSubscribersLoader extends CursorSupportUsersLoader {

    private final long mListId;
    private final long mUserId;
    private final String mScreenName, mListName;

    public UserListSubscribersLoader(final Context context, final long accountId, final long listId,
                                     final long userId, final String screenName, final String listName, final long cursor,
                                     final List<ParcelableUser> data, boolean fromUser) {
        super(context, accountId, cursor, data, fromUser);
        mListId = listId;
        mUserId = userId;
        mScreenName = screenName;
        mListName = listName;
    }

    @NonNull
    @Override
    public PageableResponseList<User> getCursoredUsers(@NonNull final Twitter twitter, final Paging paging)
            throws TwitterException {
        if (mListId > 0)
            return twitter.getUserListSubscribers(mListId, paging);
        else if (mUserId > 0)
            return twitter.getUserListSubscribers(mListName.replace(' ', '-'), mUserId, paging);
        else if (mScreenName != null)
            return twitter.getUserListSubscribers(mListName.replace(' ', '-'), mScreenName, paging);
        throw new TwitterException("list_id or list_name and user_id (or screen_name) required");
    }

}