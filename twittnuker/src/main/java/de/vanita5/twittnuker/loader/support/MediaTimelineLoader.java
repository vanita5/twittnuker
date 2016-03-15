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
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import org.apache.commons.lang3.StringUtils;

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.SearchQuery;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.model.User;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.TwitterWrapper;
import de.vanita5.twittnuker.util.Utils;

import java.util.List;

public class MediaTimelineLoader extends TwitterAPIStatusesLoader {

    private final String mUserId;
    private final String mUserScreenName;

    private User mUser;

    public MediaTimelineLoader(final Context context, final UserKey accountKey, final String userId,
                               final String screenName, final String sinceId, final String maxId,
                               final List<ParcelableStatus> data, final String[] savedStatusesArgs,
                               final int tabPosition, final boolean fromUser, boolean loadingMore) {
        super(context, accountKey, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser,
                loadingMore);
        mUserId = userId;
        mUserScreenName = screenName;
    }

    @NonNull
    @Override
    protected ResponseList<Status> getStatuses(@NonNull final Twitter twitter,
                                               @NonNull final ParcelableCredentials credentials,
                                               @NonNull final Paging paging) throws TwitterException {
        final Context context = getContext();
        switch (ParcelableAccountUtils.getAccountType(credentials)) {
            case ParcelableAccount.Type.TWITTER: {
                if (Utils.isOfficialCredentials(context, credentials)) {
                    if (mUserId != null) {
                        return twitter.getMediaTimeline(mUserId, paging);
                    }
                    if (mUserScreenName != null) {
                        return twitter.getMediaTimelineByScreenName(mUserScreenName, paging);
                    }
                } else {
                    final String screenName;
                    if (mUserScreenName != null) {
                        screenName = mUserScreenName;
                    } else {
                        if (mUser == null) {
                            mUser = TwitterWrapper.tryShowUser(twitter, mUserId, null);
                        }
                        screenName = mUser.getScreenName();
                    }
                    final SearchQuery query;
                    if (TwitterAPIFactory.isTwitterCredentials(credentials)) {
                        query = new SearchQuery("from:" + screenName + " filter:media exclude:retweets");
                    } else {
                        query = new SearchQuery("@" + screenName + " pic.twitter.com -RT");
                    }
                    query.paging(paging);
                    final ResponseList<Status> result = new ResponseList<>();
                    for (Status status : twitter.search(query)) {
                        final User user = status.getUser();
                        if (TextUtils.equals(user.getId(), mUserId) ||
                                StringUtils.endsWithIgnoreCase(user.getScreenName(), mUserScreenName)) {
                            result.add(status);
                        }
                    }
                    return result;
                }
                throw new TwitterException("Wrong user");
            }
            case ParcelableAccount.Type.FANFOU: {
                if (mUserId != null) {
                    return twitter.getPhotosUserTimeline(mUserId, paging);
                }
                if (mUserScreenName != null) {
                    return twitter.getPhotosUserTimeline(mUserScreenName, paging);
                }
                throw new TwitterException("Wrong user");
            }
        }
        throw new TwitterException("Not implemented");
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        final UserKey retweetUserId = status.is_retweet ? status.user_key : null;
        return !isMyTimeline() && InternalTwitterContentUtils.isFiltered(database, retweetUserId, status.text_plain,
                status.text_html, status.source, null, status.quoted_user_id);
    }

    private boolean isMyTimeline() {
        if (mUserId != null) {
            return getAccountKey().check(mUserId, null);
        } else {
            final String accountScreenName = DataStoreUtils.getAccountScreenName(getContext(), getAccountKey());
            return accountScreenName != null && accountScreenName.equalsIgnoreCase(mUserScreenName);
        }
    }
}