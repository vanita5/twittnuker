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

package de.vanita5.twittnuker.task.twitter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import org.mariotaku.sqliteqb.library.Expression;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Activity;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.model.ParcelableActivity;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.task.ManagedAsyncTask;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.content.ContentResolverUtils;
import de.vanita5.twittnuker.util.message.GetActivitiesTaskEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class GetActivitiesTask extends ManagedAsyncTask<Object, Object, Object> {

    protected final AsyncTwitterWrapper twitterWrapper;
    protected final long[] accountIds;
    protected final long[] maxIds;
    protected final long[] sinceIds;

    public GetActivitiesTask(AsyncTwitterWrapper twitterWrapper, String tag, long[] accountIds, long[] maxIds, long[] sinceIds) {
        super(twitterWrapper.getContext(), tag);
        this.twitterWrapper = twitterWrapper;
        this.accountIds = accountIds;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
    }

    @Override
    protected Object doInBackground(Object... params) {
        final Context context = twitterWrapper.getContext();
        final ContentResolver cr = context.getContentResolver();
        final int loadItemLimit = twitterWrapper.getPreferences().getInt(KEY_LOAD_ITEM_LIMIT);
        boolean getReadPosition = false;
        for (int i = 0; i < accountIds.length; i++) {
            final long accountId = accountIds[i];
            final boolean noItemsBefore = DataStoreUtils.getActivityCountInDatabase(context,
                    getContentUri(), accountId) <= 0;
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountId,
                    true);
            final Paging paging = new Paging();
            paging.count(loadItemLimit);
            if (maxIds != null && maxIds[i] > 0) {
                paging.maxId(maxIds[i]);
            }
            if (sinceIds != null && sinceIds[i] > 0) {
                paging.sinceId(sinceIds[i]);
                if (maxIds == null || maxIds[i] <= 0) {
                    paging.setLatestResults(true);
                    getReadPosition = true;
                }
            }
            // We should delete old activities has intersection with new items
            long[] deleteBound = new long[2];
            Arrays.fill(deleteBound, -1);
            try {
                List<ContentValues> valuesList = new ArrayList<>();
                for (Activity activity : getActivities(accountId, twitter, paging)) {
                    final ParcelableActivity parcelableActivity = new ParcelableActivity(activity, accountId, false);
                    if (deleteBound[0] < 0) {
                        deleteBound[0] = parcelableActivity.min_position;
                    } else {
                        deleteBound[0] = Math.min(deleteBound[0], parcelableActivity.min_position);
                    }
                    if (deleteBound[1] < 0) {
                        deleteBound[1] = parcelableActivity.max_position;
                    } else {
                        deleteBound[1] = Math.max(deleteBound[1], parcelableActivity.max_position);
                    }
                    final ContentValues values = ContentValuesCreator.createActivity(parcelableActivity);
                    values.put(Statuses.INSERTED_DATE, System.currentTimeMillis());
                    valuesList.add(values);
                }
                if (deleteBound[0] > 0 && deleteBound[1] > 0) {
                    Expression where = Expression.and(
                            Expression.equals(Activities.ACCOUNT_ID, accountId),
                            Expression.greaterEquals(Activities.MIN_POSITION, deleteBound[0]),
                            Expression.lesserEquals(Activities.MAX_POSITION, deleteBound[1])
                    );
                    int rowsDeleted = cr.delete(getContentUri(), where.getSQL(), null);
                    boolean insertGap = valuesList.size() >= loadItemLimit && !noItemsBefore
                            && rowsDeleted <= 0;
                    if (insertGap && !valuesList.isEmpty()) {
                        valuesList.get(valuesList.size() - 1).put(Activities.IS_GAP, true);
                    }
                }
                ContentResolverUtils.bulkInsert(cr, getContentUri(), valuesList);
                if (getReadPosition) {
                    getReadPosition(accountId, twitter);
                }
            } catch (TwitterException e) {

            }
        }
        return null;
    }

    protected abstract void getReadPosition(long accountId, Twitter twitter);

    protected abstract ResponseList<Activity> getActivities(long accountId, Twitter twitter, Paging paging) throws TwitterException;

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        bus.post(new GetActivitiesTaskEvent(getContentUri(), false, null));
    }

    protected abstract Uri getContentUri();

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        bus.post(new GetActivitiesTaskEvent(getContentUri(), true, null));
    }
}