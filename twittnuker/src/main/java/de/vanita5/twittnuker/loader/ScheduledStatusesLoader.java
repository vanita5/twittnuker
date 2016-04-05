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
import android.support.v4.content.AsyncTaskLoader;

import java.util.List;

import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ScheduledStatus;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.util.TwitterAPIFactory;

public class ScheduledStatusesLoader extends AsyncTaskLoader<List<ScheduledStatus>> {

    private final UserKey mAccountId;
    private final String mSinceId;
    private final String mMaxId;
    @ScheduledStatus.State
    private final String[] mStates;

    public ScheduledStatusesLoader(Context context, UserKey accountId, String sinceId, String maxId,
                                   @ScheduledStatus.State String[] states, List<ScheduledStatus> data) {
        super(context);
        mAccountId = accountId;
        mSinceId = sinceId;
        mMaxId = maxId;
        mStates = states;
    }


    @Override
    public List<ScheduledStatus> loadInBackground() {
        final Twitter twitter = TwitterAPIFactory.getTwitterInstance(getContext(), mAccountId, true);
        if (twitter == null) return null;
        final Paging paging = new Paging();
        if (mSinceId != null) {
            paging.setSinceId(mSinceId);
        }
        if (mMaxId != null) {
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