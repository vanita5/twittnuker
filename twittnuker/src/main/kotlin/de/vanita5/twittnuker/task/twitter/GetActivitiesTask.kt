/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.task.twitter

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.support.annotation.UiThread
import org.mariotaku.kpreferences.get
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.twitter.model.Paging
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.TwittnukerConstants.QUERY_PARAM_NOTIFY_CHANGE
import de.vanita5.twittnuker.constant.loadItemLimitKey
import de.vanita5.twittnuker.exception.AccountNotFoundException
import de.vanita5.twittnuker.extension.model.getMaxId
import de.vanita5.twittnuker.extension.model.getMaxSortId
import de.vanita5.twittnuker.extension.model.getSinceId
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.RefreshTaskParam
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.GetActivitiesTaskEvent
import de.vanita5.twittnuker.model.task.GetTimelineResult
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities
import de.vanita5.twittnuker.task.BaseAbstractTask
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.UriUtils
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import de.vanita5.twittnuker.util.sync.SyncTaskRunner
import de.vanita5.twittnuker.util.sync.TimelineSyncManager
import java.util.*

abstract class GetActivitiesTask(
        context: Context
) : BaseAbstractTask<RefreshTaskParam, List<Pair<GetTimelineResult<ParcelableActivity>?, Exception?>>,
        (Boolean) -> Unit>(context) {

    protected abstract val errorInfoKey: String

    protected abstract val contentUri: Uri

    override fun doLongOperation(param: RefreshTaskParam): List<Pair<GetTimelineResult<ParcelableActivity>?, Exception?>> {
        if (param.shouldAbort) return emptyList()
        val accountKeys = param.accountKeys.takeIf { it.isNotEmpty() } ?: return emptyList()
        val loadItemLimit = preferences[loadItemLimitKey]
        val result = accountKeys.mapIndexed { i, accountKey ->
            val noItemsBefore = DataStoreUtils.getActivitiesCount(context, contentUri, accountKey) <= 0
            val credentials = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey,
                    true) ?: throw AccountNotFoundException()
            val paging = Paging()
            paging.count(loadItemLimit)
            val maxId = param.getMaxId(i)
            val maxSortId = param.getMaxSortId(i)
            if (maxId != null) {
                paging.maxId(maxId)
            }
            val sinceId = param.getSinceId(i)
            if (sinceId != null) {
                paging.sinceId(sinceId)
                if (maxId == null) {
                    paging.setLatestResults(true)
                }
            }
            // We should delete old activities has intersection with new items
            try {
                val timelineResult = getActivities(credentials, paging)
                val storeResult = storeActivities(credentials, timelineResult.data, sinceId, maxId,
                        loadItemLimit, noItemsBefore, false)
                errorInfoStore.remove(errorInfoKey, accountKey)
                if (storeResult != 0) {
                    throw GetStatusesTask.GetTimelineException(storeResult)
                }
                return@mapIndexed Pair(timelineResult, null)
            } catch (e: MicroBlogException) {
                DebugLog.w(LOGTAG, tr = e)
                if (e.errorCode == 220) {
                    errorInfoStore[errorInfoKey, accountKey] = ErrorInfoStore.CODE_NO_ACCESS_FOR_CREDENTIALS
                } else if (e.isCausedByNetworkIssue) {
                    errorInfoStore[errorInfoKey, accountKey] = ErrorInfoStore.CODE_NETWORK_ERROR
                }
                return@mapIndexed Pair(null, e)
            } catch (e: GetStatusesTask.GetTimelineException) {
                return@mapIndexed Pair(null, e)
            }
        }
        val manager = timelineSyncManagerFactory.get()
        if (manager != null && syncPreferences.isSyncEnabled(SyncTaskRunner.SYNC_TYPE_TIMELINE_POSITIONS)) {
            if (param.isBackground) {
                syncFetchReadPosition(manager, accountKeys)
            }
        }
        return result
    }

    override fun afterExecute(handler: ((Boolean) -> Unit)?, results: List<Pair<GetTimelineResult<ParcelableActivity>?, Exception?>>) {
        context.contentResolver.notifyChange(contentUri, null)
        val exception = results.firstOrNull { it.second != null }?.second
        bus.post(GetActivitiesTaskEvent(contentUri, false, exception))
        GetStatusesTask.cacheUserRelationship(context, results)
        handler?.invoke(true)
    }

    @UiThread
    override fun beforeExecute() {
        bus.post(GetActivitiesTaskEvent(contentUri, true, null))
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getActivities(account: AccountDetails, paging: Paging): GetTimelineResult<ParcelableActivity>

    protected abstract fun syncFetchReadPosition(manager: TimelineSyncManager, accountKeys: Array<UserKey>)

    private fun storeActivities(details: AccountDetails, activities: List<ParcelableActivity>,
            sinceId: String?, maxId: String?, loadItemLimit: Int, noItemsBefore: Boolean,
            notify: Boolean): Int {
        val cr = context.contentResolver
        val deleteBound = LongArray(2) { -1 }
        val valuesList = ArrayList<ContentValues>()
        var minIdx = -1
        var minPositionKey: Long = -1
        if (!activities.isEmpty()) {
            val firstSortId = activities.first().timestamp
            val lastSortId = activities.last().timestamp
            // Get id diff of first and last item
            val sortDiff = firstSortId - lastSortId
            activities.forEachIndexed { i, activity ->
                mediaPreloader.preloadActivity(activity)
                activity.position_key = GetStatusesTask.getPositionKey(activity.timestamp,
                        activity.timestamp, lastSortId, sortDiff, i, activities.size)
                if (deleteBound[0] < 0) {
                    deleteBound[0] = activity.min_sort_position
                } else {
                    deleteBound[0] = Math.min(deleteBound[0], activity.min_sort_position)
                }
                if (deleteBound[1] < 0) {
                    deleteBound[1] = activity.max_sort_position
                } else {
                    deleteBound[1] = Math.max(deleteBound[1], activity.max_sort_position)
                }
                if (minIdx == -1 || activity < activities[minIdx]) {
                    minIdx = i
                    minPositionKey = activity.position_key
                }

                activity.inserted_date = System.currentTimeMillis()
                valuesList.add(ObjectCursor.valuesCreatorFrom(ParcelableActivity::class.java)
                        .create(activity))
            }
        }
        var olderCount = -1
        if (minPositionKey > 0) {
            olderCount = DataStoreUtils.getActivitiesCount(context, contentUri, Activities.POSITION_KEY,
                    minPositionKey, false, arrayOf(details.key))
        }
        val writeUri = UriUtils.appendQueryParameters(contentUri, QUERY_PARAM_NOTIFY_CHANGE, notify)
        if (deleteBound[0] > 0 && deleteBound[1] > 0) {
            val where = Expression.and(
                    Expression.equalsArgs(Activities.ACCOUNT_KEY),
                    Expression.greaterEquals(Activities.MIN_SORT_POSITION, deleteBound[0]),
                    Expression.lesserEquals(Activities.MAX_SORT_POSITION, deleteBound[1])
            )
            val whereArgs = arrayOf(details.key.toString())
            // First item after gap doesn't count
            val localDeleted = if (maxId != null && sinceId == null) 1 else 0
            val rowsDeleted = cr.delete(writeUri, where.sql, whereArgs) - localDeleted
            // Why loadItemLimit / 2? because it will not acting strange in most cases
            val insertGap = !noItemsBefore && olderCount > 0  && rowsDeleted <= 0 && activities.size > loadItemLimit / 2
            if (insertGap && !valuesList.isEmpty()) {
                valuesList[valuesList.size - 1].put(Activities.IS_GAP, true)
            }
        }
        // Insert previously fetched items.
        ContentResolverUtils.bulkInsert(cr, writeUri, valuesList)

        // Remove gap flag
        if (maxId != null && sinceId == null) {
            if (activities.isNotEmpty()) {
                // Only remove when actual result returned, otherwise it seems that gap is too old to load
                if (params.extraId != -1L) {
                    val noGapValues = ContentValues()
                    noGapValues.put(Activities.IS_GAP, false)
                    val noGapWhere = Expression.equals(Activities._ID, params.extraId).sql
                    cr.update(writeUri, noGapValues, noGapWhere, null)
                }
            } else {
                return GetStatusesTask.ERROR_LOAD_GAP
            }
        }
        return 0
    }
}