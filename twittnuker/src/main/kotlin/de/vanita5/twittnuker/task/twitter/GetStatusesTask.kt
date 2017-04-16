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
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.ResponseList
import de.vanita5.twittnuker.library.twitter.model.Status
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.TwittnukerConstants.QUERY_PARAM_NOTIFY_CHANGE
import de.vanita5.twittnuker.constant.loadItemLimitKey
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.RefreshTaskParam
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.GetStatusesTaskEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.AccountSupportColumns
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.task.BaseAbstractTask
import de.vanita5.twittnuker.task.cache.CacheUsersStatusesTask
import de.vanita5.twittnuker.util.*
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import java.util.*

abstract class GetStatusesTask(
        context: Context
) : BaseAbstractTask<RefreshTaskParam, List<TwitterWrapper.StatusListResponse>, (Boolean) -> Unit>(context) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    @Throws(MicroBlogException::class)
    abstract fun getStatuses(twitter: MicroBlog, paging: Paging): ResponseList<Status>

    protected abstract val contentUri: Uri

    protected abstract val errorInfoKey: String

    override fun doLongOperation(param: RefreshTaskParam): List<TwitterWrapper.StatusListResponse> {
        if (param.shouldAbort) return emptyList()
        val accountKeys = param.accountKeys
        val maxIds = param.maxIds
        val sinceIds = param.sinceIds
        val maxSortIds = param.maxSortIds
        val sinceSortIds = param.sinceSortIds
        val result = ArrayList<TwitterWrapper.StatusListResponse>()
        val loadItemLimit = preferences[loadItemLimitKey]
        var saveReadPosition = false
        for (i in 0 until accountKeys.size) {
            val accountKey = accountKeys[i]
            val details = AccountUtils.getAccountDetails(AccountManager.get(context),
                    accountKey, true) ?: continue
            val microBlog = details.newMicroBlogInstance(context = context, cls = MicroBlog::class.java)
            try {
                val paging = Paging()
                paging.count(loadItemLimit)
                val maxId: String?
                val sinceId: String?
                var maxSortId: Long = -1
                var sinceSortId: Long = -1
                if (maxIds != null && maxIds[i] != null) {
                    maxId = maxIds[i]
                    paging.maxId(maxId)
                    if (maxSortIds != null) {
                        maxSortId = maxSortIds[i]
                    }
                } else {
                    maxSortId = -1
                    maxId = null
                }
                if (sinceIds != null && sinceIds[i] != null) {
                    sinceId = sinceIds[i]
                    val sinceIdLong = sinceId.toLongOr(-1L)
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1L) {
                        paging.sinceId((sinceIdLong - 1).toString())
                    } else {
                        paging.sinceId(sinceId)
                    }
                    if (sinceSortIds != null) {
                        sinceSortId = sinceSortIds[i]
                    }
                    if (maxIds == null) {
                        paging.setLatestResults(true)
                    }
                    saveReadPosition = true
                } else {
                    sinceId = null
                }
                val statuses = getStatuses(microBlog, paging)
                val storeResult = storeStatus(accountKey, details, statuses, sinceId, maxId,
                        sinceSortId, maxSortId, loadItemLimit, false)
                if (saveReadPosition) {
                    setLocalReadPosition(accountKey, details, microBlog)
                }
                // TODO cache related data and preload
                val cacheTask = CacheUsersStatusesTask(context, accountKey, details.type, statuses)
                TaskStarter.execute(cacheTask)
                errorInfoStore.remove(errorInfoKey, accountKey.id)
                result.add(TwitterWrapper.StatusListResponse(accountKey, statuses))
                if (storeResult != 0) {
                    throw GetTimelineException(storeResult)
                }
            } catch (e: MicroBlogException) {
                DebugLog.w(LOGTAG, tr = e)
                if (e.isCausedByNetworkIssue) {
                    errorInfoStore[errorInfoKey, accountKey.id] = ErrorInfoStore.CODE_NETWORK_ERROR
                } else if (e.statusCode == 401) {
                    // Unauthorized
                }
                result.add(TwitterWrapper.StatusListResponse(accountKey, e))
            } catch (e: GetTimelineException) {
                result.add(TwitterWrapper.StatusListResponse(accountKey, e))
            }
        }
        return result
    }

    override fun afterExecute(handler: ((Boolean) -> Unit)?, result: List<TwitterWrapper.StatusListResponse>) {
        context.contentResolver.notifyChange(contentUri, null)
        val exception = AsyncTwitterWrapper.getException(result)
        bus.post(GetStatusesTaskEvent(contentUri, false, exception))
        handler?.invoke(true)
    }

    override fun beforeExecute() {
        bus.post(GetStatusesTaskEvent(contentUri, true, null))
    }

    protected abstract fun setLocalReadPosition(accountKey: UserKey, details: AccountDetails,
            twitter: MicroBlog)

    private fun storeStatus(accountKey: UserKey, details: AccountDetails,
                            statuses: List<Status>,
                            sinceId: String?, maxId: String?,
                            sinceSortId: Long, maxSortId: Long,
                            loadItemLimit: Int, notify: Boolean): Int {
        val uri = contentUri
        val writeUri = UriUtils.appendQueryParameters(uri, QUERY_PARAM_NOTIFY_CHANGE, notify)
        val resolver = context.contentResolver
        val noItemsBefore = DataStoreUtils.getStatusCount(context, uri, accountKey) <= 0
        val values = arrayOfNulls<ContentValues>(statuses.size)
        val statusIds = arrayOfNulls<String>(statuses.size)
        var minIdx = -1
        var minPositionKey: Long = -1
        var hasIntersection = false
        if (!statuses.isEmpty()) {
            val firstSortId = statuses.first().sortId
            val lastSortId = statuses.last().sortId
            // Get id diff of first and last item
            val sortDiff = firstSortId - lastSortId

            val creator = ObjectCursor.valuesCreatorFrom(ParcelableStatus::class.java)
            for (i in 0 until statuses.size) {
                val item = statuses[i]
                val status = ParcelableStatusUtils.fromStatus(item, accountKey, details.type, false,
                        profileImageSize)
                ParcelableStatusUtils.updateExtraInformation(status, details)
                status.position_key = getPositionKey(status.timestamp, status.sort_id, lastSortId,
                        sortDiff, i, statuses.size)
                status.inserted_date = System.currentTimeMillis()
                mediaPreloader.preloadStatus(status)
                values[i] = creator.create(status)
                if (minIdx == -1 || item < statuses[minIdx]) {
                    minIdx = i
                    minPositionKey = status.position_key
                }
                if (sinceId != null && item.sortId <= sinceSortId) {
                    hasIntersection = true
                }
                statusIds[i] = item.id
            }
        }
        // Delete all rows conflicting before new data inserted.
        val accountWhere = Expression.equalsArgs(AccountSupportColumns.ACCOUNT_KEY)
        val statusWhere = Expression.inArgs(Columns.Column(Statuses.STATUS_ID),
                statusIds.size)
        val deleteWhere = Expression.and(accountWhere, statusWhere).sql
        val deleteWhereArgs = arrayOf(accountKey.toString(), *statusIds)
        var olderCount = -1
        if (minPositionKey > 0) {
            olderCount = DataStoreUtils.getStatusesCount(context, preferences, uri, null,
                    Statuses.POSITION_KEY, minPositionKey, false, arrayOf(accountKey))
        }
        val rowsDeleted = resolver.delete(writeUri, deleteWhere, deleteWhereArgs)

        // Insert a gap.
        val deletedOldGap = rowsDeleted > 0 && maxId in statusIds
        val noRowsDeleted = rowsDeleted == 0
        // Why loadItemLimit / 2? because it will not acting strange in most cases
        val insertGap = minIdx != -1 && olderCount > 0 && (noRowsDeleted || deletedOldGap)
                && !noItemsBefore && !hasIntersection && statuses.size > loadItemLimit / 2
        if (insertGap) {
            values[minIdx]!!.put(Statuses.IS_GAP, true)
        }
        // Insert previously fetched items.
        ContentResolverUtils.bulkInsert(resolver, writeUri, values)

        // Remove gap flag
        if (maxId != null && sinceId == null) {
            if (statuses.isNotEmpty()) {
                // Only remove when actual result returned, otherwise it seems that gap is too old to load
                val noGapValues = ContentValues()
                noGapValues.put(Statuses.IS_GAP, false)
                val noGapWhere = Expression.and(Expression.equalsArgs(Statuses.ACCOUNT_KEY),
                        Expression.equalsArgs(Statuses.STATUS_ID)).sql
                val noGapWhereArgs = arrayOf(accountKey.toString(), maxId)
                resolver.update(writeUri, noGapValues, noGapWhere, noGapWhereArgs)
            } else {
                return ERROR_LOAD_GAP
            }
        }
        return 0
    }

    class GetTimelineException(val code: Int) : Exception() {
        fun getToastMessage(context: Context): String {
            when (code) {
                ERROR_LOAD_GAP -> return context.getString(R.string.message_toast_unable_to_load_more_statuses)
            }
            return context.getString(R.string.error_unknown_error)
        }
    }

    companion object {

        const val ERROR_LOAD_GAP = 1

        fun getPositionKey(timestamp: Long, sortId: Long, lastSortId: Long, sortDiff: Long,
                           position: Int, count: Int): Long {
            if (sortDiff == 0L) return timestamp
            val extraValue: Int
            if (sortDiff > 0) {
                // descent sorted by time
                extraValue = count - 1 - position
            } else {
                // ascent sorted by time
                extraValue = position
            }
            return timestamp + (sortId - lastSortId) * (499 - count) / sortDiff + extraValue.toLong()
        }
    }

}