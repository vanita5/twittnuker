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
import android.support.annotation.UiThread;
import android.util.Log;

import com.squareup.otto.Bus;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.SQLFunctions;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.Status;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.RefreshTaskParam;
import de.vanita5.twittnuker.model.message.GetStatusesTaskEvent;
import de.vanita5.twittnuker.provider.TwidereDataStore.AccountSupportColumns;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.task.AbstractTask;
import de.vanita5.twittnuker.task.CacheUsersStatusesTask;
import de.vanita5.twittnuker.task.util.TaskStarter;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.ErrorInfoStore;
import de.vanita5.twittnuker.util.InternalTwitterContentUtils;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.TwitterWrapper;
import de.vanita5.twittnuker.util.UriUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.content.ContentResolverUtils;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public abstract class GetStatusesTask extends AbstractTask<RefreshTaskParam,
        List<TwitterWrapper.StatusListResponse>, Object> implements Constants {

    protected final Context context;
    @Inject
    protected SharedPreferencesWrapper preferences;
    @Inject
    protected Bus bus;
    @Inject
    protected ErrorInfoStore errorInfoStore;

    public GetStatusesTask(Context context) {
        this.context = context;
        GeneralComponentHelper.build(context).inject(this);
    }

    @NonNull
    public abstract ResponseList<Status> getStatuses(Twitter twitter, Paging paging)
            throws TwitterException;

    @NonNull
    protected abstract Uri getContentUri();


    @Override
    public void afterExecute(List<TwitterWrapper.StatusListResponse> result) {
        bus.post(new GetStatusesTaskEvent(getContentUri(), false, AsyncTwitterWrapper.getException(result)));
    }

    @UiThread
    public void notifyStart() {
        bus.post(new GetStatusesTaskEvent(getContentUri(), true, null));
    }

    @Override
    public List<TwitterWrapper.StatusListResponse> doLongOperation(final RefreshTaskParam param) {
        final UserKey[] accountKeys = param.getAccountKeys();
        final String[] maxIds = param.getMaxIds();
        final String[] sinceIds = param.getSinceIds();
        final long[] maxSortIds = param.getMaxSortIds();
        final long[] sinceSortIds = param.getSinceSortIds();
        final List<TwitterWrapper.StatusListResponse> result = new ArrayList<>();
        int idx = 0;
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT, DEFAULT_LOAD_ITEM_LIMIT);
        for (final UserKey accountKey : accountKeys) {
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountKey, true);
            if (twitter == null) continue;
            try {
                final Paging paging = new Paging();
                paging.count(loadItemLimit);
                final String maxId, sinceId;
                long maxSortId = -1, sinceSortId = -1;
                if (maxIds != null && maxIds[idx] != null) {
                    maxId = maxIds[idx];
                    paging.maxId(maxId);
                    if (maxSortIds != null) {
                        maxSortId = maxSortIds[idx];
                    }
                } else {
                    maxSortId = -1;
                    maxId = null;
                }
                if (sinceIds != null && sinceIds[idx] != null) {
                    sinceId = sinceIds[idx];
                    long sinceIdLong = NumberUtils.toLong(sinceId, -1);
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1) {
                        paging.sinceId(String.valueOf(sinceIdLong - 1));
                    } else {
                        paging.sinceId(sinceId);
                    }
                    if (sinceSortIds != null) {
                        sinceSortId = sinceSortIds[idx];
                    }
                    if (maxIds == null || sinceIds[idx] == null) {
                        paging.setLatestResults(true);
                    }
                } else {
                    sinceId = null;
                }
                final List<Status> statuses = getStatuses(twitter, paging);
                InternalTwitterContentUtils.getStatusesWithQuoteData(twitter, statuses);
                storeStatus(accountKey, statuses, sinceId, maxId, sinceSortId, maxSortId,
                        loadItemLimit, true);
                // TODO cache related data and preload
                final CacheUsersStatusesTask cacheTask = new CacheUsersStatusesTask(context);
                cacheTask.setParams(new TwitterWrapper.StatusListResponse(accountKey, statuses));
                TaskStarter.execute(cacheTask);
                errorInfoStore.remove(getErrorInfoKey(), accountKey.getId());
            } catch (final TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
                if (e.isCausedByNetworkIssue()) {
                    errorInfoStore.put(getErrorInfoKey(), accountKey.getId(),
                            ErrorInfoStore.CODE_NETWORK_ERROR);
                }
                result.add(new TwitterWrapper.StatusListResponse(accountKey, e));
            }
            idx++;
        }
        return result;
    }

    @NonNull
    protected abstract String getErrorInfoKey();

    private void storeStatus(final UserKey accountKey, final List<Status> statuses,
                             final String sinceId, final String maxId,
                             final long sinceSortId, final long maxSortId,
                             int loadItemLimit, final boolean notify) {
        if (statuses == null || statuses.isEmpty() || accountKey == null) {
            return;
        }
        final Uri uri = getContentUri();
        final ContentResolver resolver = context.getContentResolver();
        final boolean noItemsBefore = DataStoreUtils.getStatusCount(context, uri, accountKey) <= 0;
        final ContentValues[] values = new ContentValues[statuses.size()];
        final String[] statusIds = new String[statuses.size()];
        int minIdx = -1;
        boolean hasIntersection = false;
        for (int i = 0, j = statuses.size(); i < j; i++) {
            final Status status = statuses.get(i);
            values[i] = ContentValuesCreator.createStatus(status, accountKey);
            values[i].put(Statuses.INSERTED_DATE, System.currentTimeMillis());
            if (minIdx == -1 || status.compareTo(statuses.get(minIdx)) < 0) {
                minIdx = i;
            }
            if (sinceId != null && status.getSortId() <= sinceSortId) {
                hasIntersection = true;
            }
            statusIds[i] = status.getId();
        }
        // Delete all rows conflicting before new data inserted.
        final Expression accountWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY);
        final Expression statusWhere = Expression.inArgs(new Columns.Column(Statuses.STATUS_ID),
                statusIds.length);
        final String countWhere = Expression.and(accountWhere, statusWhere).getSQL();
        final String[] whereArgs = new String[statusIds.length + 1];
        System.arraycopy(statusIds, 0, whereArgs, 1, statusIds.length);
        whereArgs[0] = accountKey.toString();
        final String[] projection = {SQLFunctions.COUNT()};
        final int rowsDeleted;
        final Cursor countCur = resolver.query(uri, projection, countWhere, whereArgs, null);
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
        final boolean insertGap = minIdx != -1 && (noRowsDeleted || deletedOldGap) && !noItemsBefore
                && !hasIntersection && statuses.size() >= loadItemLimit;
        if (insertGap) {
            values[minIdx].put(Statuses.IS_GAP, true);
        }
        // Insert previously fetched items.
        final Uri insertUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY, notify);
        ContentResolverUtils.bulkInsert(resolver, insertUri, values);

    }


}