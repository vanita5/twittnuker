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

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.SearchQuery;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.TwitterContentUtils;

import java.util.List;

public class TweetSearchLoader extends TwitterAPIStatusesLoader {

    @NonNull
    private final String mQuery;
    private final boolean mGapEnabled;
    private final boolean mTwitterOptimizedSearches;

    public TweetSearchLoader(final Context context, final long accountId, @NonNull final String query,
                             final long sinceId, final long maxId, final List<ParcelableStatus> data,
                             final String[] savedStatusesArgs, final int tabPosition, boolean fromUser,
                             boolean makeGap, boolean twitterOptimizedSearches) {
        super(context, accountId, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser);
        mQuery = query;
        mGapEnabled = makeGap;
        mTwitterOptimizedSearches = twitterOptimizedSearches;
    }

    @NonNull
    @Override
    public List<Status> getStatuses(@NonNull final Twitter twitter, final Paging paging) throws TwitterException {
        final SearchQuery query = new SearchQuery(processQuery(mQuery));
        query.paging(paging);
        return twitter.search(query);
    }

    @NonNull
    protected String processQuery(@NonNull final String query) {
        if (mTwitterOptimizedSearches) {
            return String.format("%s exclude:retweets", query);
        }
        return query;
    }

    @Override
    protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        return TwitterContentUtils.isFiltered(database, status, true);
    }

    @Override
    protected boolean isGapEnabled() {
        return mGapEnabled;
    }

    public boolean isTwitterOptimizedSearches() {
        return mTwitterOptimizedSearches;
    }
}