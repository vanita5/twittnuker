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
import android.support.v4.content.AsyncTaskLoader;

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ScheduledStatus;
import de.vanita5.twittnuker.util.TwitterAPIFactory;

import java.util.List;

public class ScheduledStatusesLoader extends AsyncTaskLoader<List<ScheduledStatus>> {

    private final long mAccountId;
    private final long mSinceId;
    private final long mMaxId;
    @ScheduledStatus.State
    private final String[] mStates;

    public ScheduledStatusesLoader(Context context, long accountId, long sinceId, long maxId,
                                   @ScheduledStatus.State String[] states, List<ScheduledStatus> data) {
        super(context);
        mAccountId = accountId;
        mSinceId = sinceId;
        mMaxId = maxId;
        mStates = states;
    }


    @Override
    public List<ScheduledStatus> loadInBackground() {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId, accountHost, true);
        final Paging paging = new Paging();
        if (mSinceId > 0) {
            paging.setSinceId(mSinceId);
        }
        if (mMaxId > 0) {
            paging.setMaxId(mMaxId);
        }
        try {
            return twitter.getScheduledStatuses(paging, mStates);
        } catch (TwitterException e) {
            return null;
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}