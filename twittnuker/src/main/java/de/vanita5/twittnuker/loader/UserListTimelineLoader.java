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

package de.vanita5.twittnuker.loader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import de.vanita5.twittnuker.library.MicroBlog;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.Paging;
import de.vanita5.twittnuker.library.twitter.model.ResponseList;
import de.vanita5.twittnuker.library.twitter.model.Status;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;

import java.util.List;

public class UserListTimelineLoader extends MicroBlogAPIStatusesLoader {

    private final UserKey mUserKey;
    private final String mScreenName, mListName;
    private final String mListId;

    public UserListTimelineLoader(final Context context, final UserKey accountKey, final String listId,
                                  final UserKey userKey, final String screenName, final String listName,
                                  final String sinceId, final String maxId, final List<ParcelableStatus> data,
                                  final String[] savedStatusesArgs, final int tabPosition, boolean fromUser, boolean loadingMore) {
        super(context, accountKey, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser, loadingMore);
        mListId = listId;
        mUserKey = userKey;
        mScreenName = screenName;
        mListName = listName;
    }

    @NonNull
    @Override
    protected ResponseList<Status> getStatuses(@NonNull final MicroBlog microBlog, @NonNull ParcelableCredentials credentials, @NonNull final Paging paging) throws MicroBlogException {
        if (mListId != null)
            return microBlog.getUserListStatuses(mListId, paging);
        else if (mListName == null)
            throw new MicroBlogException("No list name or id given");
        else if (mUserKey != null)
            return microBlog.getUserListStatuses(mListName.replace(' ', '-'), mUserKey.getId(), paging);
        else if (mScreenName != null)
            return microBlog.getUserListStatuses(mListName.replace(' ', '-'), mScreenName, paging);
        throw new MicroBlogException("User id or screen name is required for list name");
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        return InternalTwitterContentUtils.isFiltered(database, status, true);
    }

}