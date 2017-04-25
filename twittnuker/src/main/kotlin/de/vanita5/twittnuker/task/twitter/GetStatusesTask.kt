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
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Paging
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.TwittnukerConstants.QUERY_PARAM_NOTIFY_CHANGE
import de.vanita5.twittnuker.constant.loadItemLimitKey
import de.vanita5.twittnuker.extension.model.api.applyLoadLimit
import de.vanita5.twittnuker.extension.model.getMaxId
import de.vanita5.twittnuker.extension.model.getMaxSortId
import de.vanita5.twittnuker.extension.model.getSinceId
import de.vanita5.twittnuker.extension.model.getSinceSortId
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.RefreshTaskParam
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.GetStatusesTaskEvent
import de.vanita5.twittnuker.model.task.GetTimelineResult
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.AccountSupportColumns
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.task.BaseAbstractTask
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.ErrorInfoStore
import de.vanita5.twittnuker.util.UriUtils
import de.vanita5.twittnuker.util.content.ContentResolverUtils

abstract class GetStatusesTask(
        context: Context
) : BaseAbstractTask<RefreshTaskParam, List<GetTimelineResult?>, (Boolean) -> Unit>(context) {

    protected abstract val contentUri: Uri

    protected abstract val errorInfoKey: String

    override fun doLongOperation(param: RefreshTaskParam): List<GetTimelineResult?> {
        if (param.shouldAbort) return emptyList()
        val accountKeys = param.accountKeys
        val loadItemLimit = preferences[loadItemLimitKey]
        var saveReadPosition = false
        return accountKeys.mapIndexed { i, accountKey ->
            val account = AccountUtils.getAccountDetails(AccountManager.get(context),
                    accountKey, true) ?: return@mapIndexed null
            try {
                val paging = Paging()
                paging.applyLoadLimit(account, loadItemLimit)
                val maxId = param.getMaxId(i)
                val sinceId = param.getSinceId(i)
                val maxSortId = param.getMaxSortId(i)
                val sinceSortId = param.getSinceSortId(i)
                if (maxId != null) {
                    paging.maxId(maxId)
                }
                if (sinceId != null) {
                    val sinceIdLong = sinceId.toLongOr(-1L)
                    //TODO handle non-twitter case
                    if (sinceIdLong != -1L) {
                        paging.sinceId((sinceIdLong - 1).toString())
                    } else {
                        paging.sinceId(sinceId)
                    }

                    if (maxId == null) {
                        paging.setLatestResults(true)
                    }
                    saveReadPosition = true
                }
                val statuses = getStatuses(account, paging)
                val storeResult = storeStatus(account, statuses, sinceId, maxId, sinceSortId,
                        maxSortId, loadItemLimit, false)
                if (saveReadPosition) {
                    setLocalReadPosition(accountKey, account)
                }
                // TODO cache related data and preload
                errorInfoStore.remove(errorInfoKey, accountKey.id)
                if (storeResult != 0) {
                    throw GetTimelineException(storeResult)
                }
                return@mapIndexed GetTimelineResult(null)
            } catch (e: MicroBlogException) {
                DebugLog.w(LOGTAG, tr = e)
                if (e.isCausedByNetworkIssue) {
                    errorInfoStore[errorInfoKey, accountKey.id] = ErrorInfoStore.CODE_NETWORK_ERROR
                } else if (e.statusCode == 401) {
                    // Unauthorized
                }
                return@mapIndexed GetTimelineResult(e)
            } catch (e: GetTimelineException) {
                return@mapIndexed GetTimelineResult(e)
            }
        }
    }

    override fun afterExecute(handler: ((Boolean) -> Unit)?, result: List<GetTimelineResult?>) {
        context.contentResolver.notifyChange(contentUri, null)
        val exception = result.firstOrNull { it?.exception != null }?.exception
        bus.post(GetStatusesTaskEvent(contentUri, false, exception))
        handler?.invoke(true)
    }

    override fun beforeExecute() {
        bus.post(GetStatusesTaskEvent(contentUri, true, null))
    }

    @Throws(MicroBlogException::class)
    protected abstract fun getStatuses(account: AccountDetails, paging: Paging): List<ParcelableStatus>

    protected abstract fun setLocalReadPosition(accountKey: UserKey, details: AccountDetails)

    private fun storeStatus(account: AccountDetails, statuses: List<ParcelableStatus>,
            sinceId: String?, maxId: String?, sinceSortId: Long, maxSortId: Long,
            loadItemLimit: Int, notify: Boolean): Int {
        val accountKey = account.key
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
            val firstSortId = statuses.first().sort_id
            val lastSortId = statuses.last().sort_id
            // Get id diff of first and last item
            val sortDiff = firstSortId - lastSortId

            val creator = ObjectCursor.valuesCreatorFrom(ParcelableStatus::class.java)
            statuses.forEachIndexed { i, status ->
                status.position_key = getPositionKey(status.timestamp, status.sort_id, lastSortId,
                        sortDiff, i, statuses.size)
                status.inserted_date = System.currentTimeMillis()
                mediaPreloader.preloadStatus(status)
                values[i] = creator.create(status)
                if (minIdx == -1 || status < statuses[minIdx]) {
                    minIdx = i
                    minPositionKey = status.position_key
                }
                if (sinceId != null && status.sort_id <= sinceSortId) {
                    hasIntersection = true
                }
                statusIds[i] = status.id
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