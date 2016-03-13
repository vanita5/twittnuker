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

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.SearchQuery;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.util.ParcelableCredentialsUtils;
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;
import de.vanita5.twittnuker.util.Nullables;
import de.vanita5.twittnuker.util.ParcelUtils;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ConversationLoader extends TwitterAPIStatusesLoader {

    @NonNull
    private final ParcelableStatus mStatus;
    private boolean mCanLoadAllReplies;

    public ConversationLoader(final Context context, @NonNull final ParcelableStatus status,
                              final long sinceId, final long maxId, final List<ParcelableStatus> data,
                              final boolean fromUser) {
        super(context, status.account_key, sinceId, maxId, data, null, -1, fromUser);
        mStatus = Nullables.assertNonNull(ParcelUtils.clone(status));
        ParcelableStatusUtils.makeOriginalStatus(mStatus);
    }

    @NonNull
    @Override
    public List<Status> getStatuses(@NonNull final Twitter twitter, final Paging paging) throws TwitterException {
        mCanLoadAllReplies = false;
        final ParcelableStatus status = mStatus;
        final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(getContext(), getAccountKey());
        if (credentials == null) throw new TwitterException("Null credentials");
        if (Utils.isOfficialCredentials(getContext(), credentials)) {
            mCanLoadAllReplies = true;
            return twitter.showConversation(status.id, paging);
        } else if (TwitterAPIFactory.isStatusNetCredentials(credentials)) {
            mCanLoadAllReplies = true;
            return twitter.getStatusNetConversation(status.id, paging);
        }
        final List<Status> statuses = new ArrayList<>();
        final long maxId = getMaxId(), sinceId = getSinceId();
        final boolean noSinceMaxId = maxId <= 0 && sinceId <= 0;
        // Load conversations
        if ((maxId > 0 && maxId < status.id) || noSinceMaxId) {
            long inReplyToId = maxId > 0 ? maxId : status.in_reply_to_status_id;
            int count = 0;
            while (inReplyToId > 0 && count < 10) {
                final Status item = twitter.showStatus(inReplyToId);
                inReplyToId = item.getInReplyToStatusId();
                statuses.add(item);
                count++;
            }
        }
        // Load replies
        if ((sinceId > 0 && sinceId > status.id) || noSinceMaxId) {
            SearchQuery query = new SearchQuery();
            if (TwitterAPIFactory.isTwitterCredentials(credentials)) {
                query.query("to:" + status.user_screen_name);
            } else {
                query.query("@" + status.user_screen_name);
            }
            query.sinceId(sinceId > 0 ? sinceId : status.id);
            try {
                for (Status item : twitter.search(query)) {
                    if (item.getInReplyToStatusId() == status.id) {
                        statuses.add(item);
                    }
                }
            } catch (TwitterException e) {
                // Ignore for now
            }
        }
        return statuses;
    }

    public boolean canLoadAllReplies() {
        return mCanLoadAllReplies;
    }

    @WorkerThread
    @Override
    protected boolean shouldFilterStatus(SQLiteDatabase database, ParcelableStatus status) {
        return InternalTwitterContentUtils.isFiltered(database, status, false);
    }

}