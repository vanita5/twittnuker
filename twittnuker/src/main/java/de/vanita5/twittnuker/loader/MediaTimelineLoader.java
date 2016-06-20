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
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;

import de.vanita5.twittnuker.library.MicroBlog;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.Paging;
import de.vanita5.twittnuker.library.twitter.model.ResponseList;
import de.vanita5.twittnuker.library.twitter.model.SearchQuery;
import de.vanita5.twittnuker.library.twitter.model.Status;
import de.vanita5.twittnuker.library.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;
import de.vanita5.twittnuker.util.MicroBlogAPIFactory;
import de.vanita5.twittnuker.util.TwitterWrapper;
import de.vanita5.twittnuker.util.Utils;

import java.util.List;


public class MediaTimelineLoader extends MicroBlogAPIStatusesLoader {

    @Nullable
    private final UserKey mUserKey;
    @Nullable
    private final String mUserScreenName;

    private User mUser;

    public MediaTimelineLoader(final Context context, final UserKey accountKey, @Nullable final UserKey userKey,
                               @Nullable final String screenName, final String sinceId, final String maxId,
                               final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                               final int tabPosition, final boolean fromUser, boolean loadingMore) {
        super(context, accountKey, sinceId, maxId, -1, data, savedStatusesArgs, tabPosition, fromUser,
                loadingMore);
        mUserKey = userKey;
        mUserScreenName = screenName;
    }

    @NonNull
    @Override
    protected ResponseList<Status> getStatuses(@NonNull final MicroBlog microBlog,
                                               @NonNull final ParcelableCredentials credentials,
                                               @NonNull final Paging paging) throws MicroBlogException {
        final Context context = getContext();
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.TWITTER: {
                if (Utils.isOfficialCredentials(context, credentials)) {
                    if (mUserKey != null) {
                        return microBlog.getMediaTimeline(mUserKey.getId(), paging);
                    }
                    if (mUserScreenName != null) {
                        return microBlog.getMediaTimelineByScreenName(mUserScreenName, paging);
                    }
                } else {
                    final String screenName;
                    if (mUserScreenName != null) {
                        screenName = mUserScreenName;
                    } else if (mUserKey != null) {
                        if (mUser == null) {
                            mUser = TwitterWrapper.tryShowUser(microBlog, mUserKey.getId(), null,
                                    credentials.account_type);
                        }
                        screenName = mUser.getScreenName();
                    } else {
                        throw new MicroBlogException("Invalid parameters");
                    }
                    final SearchQuery query;
                    if (MicroBlogAPIFactory.isTwitterCredentials(credentials)) {
                        query = new SearchQuery("from:" + screenName + " filter:media exclude:retweets");
                    } else {
                        query = new SearchQuery("@" + screenName + " pic.twitter.com -RT");
                    }
                    query.paging(paging);
                    final ResponseList<Status> result = new ResponseList<>();
                    for (Status status : microBlog.search(query)) {
                        final User user = status.getUser();
                        if ((mUserKey != null && TextUtils.equals(user.getId(), mUserKey.getId())) ||
                                StringUtils.endsWithIgnoreCase(user.getScreenName(), mUserScreenName)) {
                            result.add(status);
                        }
                    }
                    return result;
                }
                throw new MicroBlogException("Wrong user");
            }
            case ParcelableAccount.Type.FANFOU: {
                if (mUserKey != null) {
                    return microBlog.getPhotosUserTimeline(mUserKey.getId(), paging);
                }
                if (mUserScreenName != null) {
                    return microBlog.getPhotosUserTimeline(mUserScreenName, paging);
                }
                throw new MicroBlogException("Wrong user");
            }
        }
        throw new MicroBlogException("Not implemented");
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        final UserKey retweetUserId = status.is_retweet ? status.user_key : null;
        return !isMyTimeline() && InternalTwitterContentUtils.isFiltered(database, retweetUserId,
                status.text_plain, status.quoted_text_plain, status.spans, status.quoted_spans,
                status.source, status.quoted_source, null, status.quoted_user_key);
    }

    private boolean isMyTimeline() {
        final UserKey accountKey = getAccountKey();
        if (accountKey == null) return false;
        if (mUserKey != null) {
            return mUserKey.maybeEquals(accountKey);
        } else {
            final String accountScreenName = DataStoreUtils.getAccountScreenName(getContext(), accountKey);
            return accountScreenName != null && accountScreenName.equalsIgnoreCase(mUserScreenName);
        }
    }
}