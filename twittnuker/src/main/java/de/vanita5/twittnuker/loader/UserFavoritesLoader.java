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

import java.util.List;

public class UserFavoritesLoader extends MicroBlogAPIStatusesLoader {

    private final UserKey mUserKey;
    private final String mUserScreenName;

    public UserFavoritesLoader(final Context context, final UserKey accountKey, final UserKey userKey,
                               final String screenName, final String sinceId, final String maxId,
                               final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                               final int tabPosition, boolean fromUser, boolean loadingMore) {
        super(context, accountKey, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser,
                loadingMore);
        mUserKey = userKey;
        mUserScreenName = screenName;
    }

    @NonNull
    @Override
    public ResponseList<Status> getStatuses(@NonNull final MicroBlog microBlog, @NonNull ParcelableCredentials credentials, @NonNull final Paging paging) throws MicroBlogException {
        if (mUserKey != null) {
            return microBlog.getFavorites(mUserKey.getId(), paging);
        } else if (mUserScreenName != null) {
            return microBlog.getFavoritesByScreenName(mUserScreenName, paging);
        }
        throw new MicroBlogException("Null user");
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        return false;
    }
}