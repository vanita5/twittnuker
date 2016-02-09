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
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.RawItemArray;
import org.mariotaku.sqliteqb.library.SQLFunctions;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.task.CacheUsersStatusesTask;
import de.vanita5.twittnuker.task.ManagedAsyncTask;
import de.vanita5.twittnuker.util.AsyncTaskUtils;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.ErrorInfoStore;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.TwitterContentUtils;
import de.vanita5.twittnuker.util.TwitterWrapper;
import de.vanita5.twittnuker.util.UriUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.content.ContentResolverUtils;
import de.vanita5.twittnuker.util.message.GetStatusesTaskEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class GetStatusesTask extends ManagedAsyncTask<Object, TwitterWrapper.TwitterListResponse<Status>, List<TwitterWrapper.StatusListResponse>> {

    private final long[] accountIds, maxIds, sinceIds;
    private final AsyncTwitterWrapper twitterWrapper;

    public GetStatusesTask(AsyncTwitterWrapper twitterWrapper, final long[] accountIds,
                           final long[] maxIds, final long[] sinceIds, final String tag) {
        super(twitterWrapper.getContext(), tag);
        this.twitterWrapper = twitterWrapper;
        this.accountIds = accountIds;
        this.maxIds = maxIds;
        this.sinceIds = sinceIds;
    }

    @NonNull
    public abstract ResponseList<de.vanita5.twittnuker.api.twitter.model.Status> getStatuses(Twitter twitter, Paging paging)
            throws TwitterException;

    @NonNull
    protected abstract Uri getDatabaseUri();

    final boolean isMaxIdsValid() {
        return maxIds != null && maxIds.length == accountIds.length;
    }

    final boolean isSinceIdsValid() {
        return sinceIds != null && sinceIds.length == accountIds.length;
    }

    private void storeStatus(long accountId, List<de.vanita5.twittnuker.api.twitter.model.Status> statuses,
                             long sinceId, long maxId, boolean notify, int loadItemLimit) {
        if (statuses == null || statuses.isEmpty() || accountId <= 0) {
            return;
        }
        final Uri uri = getDatabaseUri();
        final Context context = twitterWrapper.getContext();
        final ContentResolver resolver = context.getContentResolver();
        final boolean noItemsBefore = DataStoreUtils.getStatusCount(context, uri, accountId) <= 0;
        final ContentValues[] values = new ContentValues[statuses.size()];
        final long[] statusIds = new long[statuses.size()];
        long minId = -1;
        int minIdx = -1;
        boolean hasIntersection = false;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final de.vanita5.twittnuker.api.twitter.model.Status status = statuses.get(i);
            values[i] = ContentValuesCreator.createStatus(status, accountId);
            values[i].put(Statuses.INSERTED_DATE, System.currentTimeMillis());
            final long id = status.getId();
            if (sinceId > 0 && id <= sinceId) {
                hasIntersection = true;
            }
            if (minId == -1 || id < minId) {
                minId = id;
                minIdx = i;
            }
            statusIds[i] = id;
        }
        // Delete all rows conflicting before new data inserted.
        final Expression accountWhere = Expression.equals(Statuses.ACCOUNT_ID, accountId);
        final Expression statusWhere = Expression.in(new Columns.Column(Statuses.STATUS_ID),
                new RawItemArray(statusIds));
        final String countWhere = Expression.and(accountWhere, statusWhere).getSQL();
        final String[] projection = {SQLFunctions.COUNT()};
        final int rowsDeleted;
        final Cursor countCur = resolver.query(uri, projection, countWhere, null, null);
        try {
            if (countCur != null && countCur.moveToFirst()) {
                rowsDeleted = countCur.getInt(0);
            } else {
                rowsDeleted = 0;
            }
        } finally {
            Utils.closeSilently(countCur);
        }

        // Insert a gap.
        final boolean deletedOldGap = rowsDeleted > 0 && ArrayUtils.contains(statusIds, maxId);
        final boolean noRowsDeleted = rowsDeleted == 0;
        final boolean insertGap = minId > 0 && (noRowsDeleted || deletedOldGap) && !noItemsBefore
                && !hasIntersection;
        if (insertGap && minIdx != -1) {
            values[minIdx].put(Statuses.IS_GAP, true);
        }
        // Insert previously fetched items.
        final Uri insertUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, notify);
        ContentResolverUtils.bulkInsert(resolver, insertUri, values);

    }

    @SafeVarargs
    @Override
    protected final void onProgressUpdate(TwitterWrapper.TwitterListResponse<de.vanita5.twittnuker.api.twitter.model.Status>... values) {
        AsyncTaskUtils.executeTask(new CacheUsersStatusesTask(twitterWrapper.getContext()), values);
    }


    @Override
    protected void onPostExecute(List<TwitterWrapper.StatusListResponse> result) {
        super.onPostExecute(result);
        bus.post(new GetStatusesTaskEvent(getDatabaseUri(), false, AsyncTwitterWrapper.getException(result)));
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        bus.post(new GetStatusesTaskEvent(getDatabaseUri(), true, null));
    }

    @Override
    protected List<TwitterWrapper.StatusListResponse> doInBackground(final Object... params) {
        final List<TwitterWrapper.StatusListResponse> result = new ArrayList<>();
        if (accountIds == null) return result;
        int idx = 0;
        final SharedPreferencesWrapper preferences = twitterWrapper.getPreferences();
        final Context context = twitterWrapper.getContext();
        final ErrorInfoStore errorInfoStore = twitterWrapper.getErrorInfoStore();
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        for (final long accountId : accountIds) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountId, true);
            if (twitter == null) continue;
            try {
                final Paging paging = new Paging();
                paging.count(loadItemLimit);
                final long maxId, sinceId;
                if (maxIds != null && maxIds[idx] > 0) {
                    maxId = maxIds[idx];
                    paging.maxId(maxId);
                } else {
                    maxId = -1;
                }
                if (sinceIds != null && sinceIds[idx] > 0) {
                    sinceId = sinceIds[idx];
                    paging.sinceId(sinceId - 1);
                    if (maxIds == null || sinceIds[idx] <= 0) {
                        paging.setLatestResults(true);
                    }
                } else {
                    sinceId = -1;
                }
                final List<de.vanita5.twittnuker.api.twitter.model.Status> statuses = getStatuses(twitter, paging);
                TwitterContentUtils.getStatusesWithQuoteData(twitter, statuses);
                storeStatus(accountId, statuses, sinceId, maxId, true, loadItemLimit);
                publishProgress(new TwitterWrapper.StatusListResponse(accountId, statuses));
                errorInfoStore.remove(getErrorInfoKey(), accountId);
            } catch (final TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
                if (e.isCausedByNetworkIssue()) {
                    errorInfoStore.put(getErrorInfoKey(), accountId,
                            ErrorInfoStore.CODE_NETWORK_ERROR);
                }
                result.add(new TwitterWrapper.StatusListResponse(accountId, e));
            }
            idx++;
        }
        return result;
    }

    @NonNull
    protected abstract String getErrorInfoKey();

}