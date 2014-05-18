/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import java.util.List;

import de.vanita5.twittnuker.model.ParcelableStatus;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import static de.vanita5.twittnuker.util.Utils.isFiltered;

public class RetweetsOfMeLoader extends Twitter4JStatusesLoader {

	private int mTotalItemsCount;

	public RetweetsOfMeLoader(final Context context, final long accountId, final long maxId, final long sinceId,
							  final List<ParcelableStatus> data, final String[] savedStatusesArgs, final int tabPosition) {
		super(context, accountId, maxId, sinceId, data, savedStatusesArgs, tabPosition);
	}

	public int getTotalItemsCount() {
		return mTotalItemsCount;
	}

	@Override
	protected ResponseList<Status> getStatuses(final Twitter twitter, final Paging paging) throws TwitterException {
		if (twitter == null) return null;
		final ResponseList<Status> statuses = twitter.getRetweetsOfMe(paging);
		if (mTotalItemsCount == -1 && !statuses.isEmpty()) {
			final User user = statuses.get(0).getUser();
			if (user != null) {
				mTotalItemsCount = user.getStatusesCount();
			}
		}
		return statuses;
	}

	@Override
	protected boolean shouldFilterStatus(final SQLiteDatabase database, final ParcelableStatus status) {
		return isFiltered(database, -1, status.text_plain, status.text_html, status.source, -1);
	}
}