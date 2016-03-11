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
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import com.squareup.otto.Bus;

import org.mariotaku.sqliteqb.library.Expression;

import de.vanita5.twittnuker.BuildConfig;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Activity;
import de.vanita5.twittnuker.api.twitter.model.Paging;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableActivity;
import de.vanita5.twittnuker.model.RefreshTaskParam;
import de.vanita5.twittnuker.model.message.GetActivitiesTaskEvent;
import de.vanita5.twittnuker.model.util.ParcelableActivityUtils;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses;
import de.vanita5.twittnuker.task.AbstractTask;
import de.vanita5.twittnuker.util.ContentValuesCreator;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.ErrorInfoStore;
import de.vanita5.twittnuker.util.ReadStateManager;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.TwitterAPIFactory;
import de.vanita5.twittnuker.util.content.ContentResolverUtils;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public abstract class GetActivitiesTask extends AbstractTask<RefreshTaskParam, Object, Object> implements Constants {

    protected final Context context;
    @Inject
    protected SharedPreferencesWrapper preferences;
    @Inject
    protected Bus bus;
    @Inject
    protected ErrorInfoStore errorInfoStore;
    @Inject
    protected ReadStateManager readStateManager;

    public GetActivitiesTask(Context context) {
        this.context = context;
        GeneralComponentHelper.build(context).inject(this);
    }

    @Override
    public Object doLongOperation(RefreshTaskParam param) {
        final AccountKey[] accountIds = param.getAccountKeys();
        final long[] maxIds = param.getMaxIds();
        final long[] sinceIds = param.getSinceIds();
        final ContentResolver cr = context.getContentResolver();
        final int loadItemLimit = preferences.getInt(KEY_LOAD_ITEM_LIMIT);
        boolean saveReadPosition = false;
        for (int i = 0; i < accountIds.length; i++) {
            final AccountKey accountKey = accountIds[i];
            final boolean noItemsBefore = DataStoreUtils.getActivitiesCount(context, getContentUri(),
                    accountKey) <= 0;
            final Twitter twitter = TwitterAPIFactory.getTwitterInstance(context, accountKey, true);
            if (twitter == null) continue;
            final Paging paging = new Paging();
            paging.count(loadItemLimit);
            if (maxIds != null && maxIds[i] > 0) {
                paging.maxId(maxIds[i]);
            }
            if (sinceIds != null && sinceIds[i] > 0) {
                paging.sinceId(sinceIds[i]);
                if (maxIds == null || maxIds[i] <= 0) {
                    paging.setLatestResults(true);
                    saveReadPosition = true;
                }
            }
            // We should delete old activities has intersection with new items
            try {
                final ResponseList<Activity> activities = getActivities(twitter, accountKey, paging);
                storeActivities(cr, loadItemLimit, accountKey, noItemsBefore, activities);
//                if (saveReadPosition && TwitterAPIFactory.isOfficialTwitterInstance(context, twitter)) {
                if (saveReadPosition) {
                    saveReadPosition(accountKey, twitter);
                }
                errorInfoStore.remove(getErrorInfoKey(), accountKey);
            } catch (TwitterException e) {
                if (BuildConfig.DEBUG) {
                    Log.w(LOGTAG, e);
                }
                if (e.getErrorCode() == 220) {
                    errorInfoStore.put(getErrorInfoKey(), accountKey,
                            ErrorInfoStore.CODE_NO_ACCESS_FOR_CREDENTIALS);
                } else if (e.isCausedByNetworkIssue()) {
                    errorInfoStore.put(getErrorInfoKey(), accountKey,
                            ErrorInfoStore.CODE_NETWORK_ERROR);
                }
            }
        }
        return null;
    }

    @NonNull
    protected abstract String getErrorInfoKey();

    private void storeActivities(ContentResolver cr, int loadItemLimit, AccountKey accountKey,
                                 boolean noItemsBefore, ResponseList<Activity> activities) {
        long[] deleteBound = new long[2];
        Arrays.fill(deleteBound, -1);
        List<ContentValues> valuesList = new ArrayList<>();
        for (Activity activity : activities) {
            final ParcelableActivity parcelableActivity = ParcelableActivityUtils.fromActivity(activity,
                    accountKey, false);
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
                    Expression.equals(Activities.ACCOUNT_ID, accountKey),
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
    }

    protected abstract void saveReadPosition(long accountId, Twitter twitter);

    protected abstract ResponseList<Activity> getActivities(@NonNull final Twitter twitter,
                                                            final long accountId, final Paging paging) throws TwitterException;

    @Override
    public void afterExecute(Object result) {
        bus.post(new GetActivitiesTaskEvent(getContentUri(), false, null));
    }

    protected abstract Uri getContentUri();

    @UiThread
    @Override
    public void beforeExecute() {
        bus.post(new GetActivitiesTaskEvent(getContentUri(), true, null));
    }
}