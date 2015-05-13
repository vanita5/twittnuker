/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import de.vanita5.twittnuker.model.ParcelableStatus;

import java.util.List;

import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.SearchQuery;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.model.TwitterException;

import static de.vanita5.twittnuker.util.Utils.isFiltered;

public class TweetSearchLoader extends TwitterAPIStatusesLoader {

	private final String mQuery;
    private final boolean mGapEnabled;

    public TweetSearchLoader(final Context context, final long accountId, final String query,
                             final long sinceId, final long maxId, final List<ParcelableStatus> data,
                             final String[] savedStatusesArgs, final int tabPosition, boolean fromUser,
                             boolean makeGap) {
        super(context, accountId, sinceId, maxId, data, savedStatusesArgs, tabPosition, fromUser);
		mQuery = query;
        mGapEnabled = makeGap;
	}

    @NonNull
	@Override
    public List<Status> getStatuses(@NonNull final Twitter twitter, final Paging paging) throws TwitterException {
		final SearchQuery query = new SearchQuery(processQuery(mQuery));
        query.setCount(paging.getCount());
		if (paging.getMaxId() > 0) {
			query.setMaxId(paging.getMaxId());
		}
        return twitter.search(query);
	}

	protected String processQuery(final String query) {
		return String.format("%s", query);
	}

	@Override
	protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
        return isFiltered(database, status, true);
	}

    @Override
    protected boolean isGapEnabled() {
        return mGapEnabled;
    }
}