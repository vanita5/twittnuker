/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.task

import android.accounts.AccountManager
import android.content.ContentValues
import android.content.Context
import org.apache.commons.collections.primitives.ArrayIntList
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.SingleResponse
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.FavoriteTaskEvent
import de.vanita5.twittnuker.model.event.StatusListChangedEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils
import de.vanita5.twittnuker.provider.TwidereDataStore
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.AsyncTwitterWrapper.Companion.calculateHashCode
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.updateActivityStatus

class DestroyFavoriteTask(
        context: Context,
        private val accountKey: UserKey,
        private val statusId: String
) : BaseAbstractTask<Any?, SingleResponse<ParcelableStatus>, Any?>(context) {
    override fun doLongOperation(params: Any?): SingleResponse<ParcelableStatus> {
        val resolver = context.contentResolver
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
                ?: return SingleResponse.getInstance<ParcelableStatus>()
        val microBlog = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        try {
            val result: ParcelableStatus
            when (details.type) {
                AccountType.FANFOU -> {
                    result = ParcelableStatusUtils.fromStatus(microBlog.destroyFanfouFavorite(statusId),
                            accountKey, false)
                }
                else -> {
                    result = ParcelableStatusUtils.fromStatus(microBlog.destroyFavorite(statusId),
                            accountKey, false)
                }
            }
            val values = ContentValues()
            values.put(TwidereDataStore.Statuses.IS_FAVORITE, false)
            values.put(TwidereDataStore.Statuses.FAVORITE_COUNT, result.favorite_count - 1)
            values.put(TwidereDataStore.Statuses.RETWEET_COUNT, result.retweet_count)
            values.put(TwidereDataStore.Statuses.REPLY_COUNT, result.reply_count)

            val where = Expression.and(Expression.equalsArgs(TwidereDataStore.Statuses.ACCOUNT_KEY),
                    Expression.or(Expression.equalsArgs(TwidereDataStore.Statuses.STATUS_ID),
                            Expression.equalsArgs(TwidereDataStore.Statuses.RETWEET_ID)))
            val whereArgs = arrayOf(accountKey.toString(), statusId, statusId)
            for (uri in DataStoreUtils.STATUSES_URIS) {
                resolver.update(uri, values, where.sql, whereArgs)
            }

            updateActivityStatus(resolver, accountKey, statusId) { activity ->
                val statusesMatrix = arrayOf(activity.target_statuses, activity.target_object_statuses)
                for (statusesArray in statusesMatrix) {
                    if (statusesArray == null) continue
                    for (status in statusesArray) {
                        if (result.id != status.id) continue
                        status.is_favorite = false
                        status.reply_count = result.reply_count
                        status.retweet_count = result.retweet_count
                        status.favorite_count = result.favorite_count - 1
                    }
                }
            }
            return SingleResponse.getInstance(result)
        } catch (e: MicroBlogException) {
            return SingleResponse.getInstance<ParcelableStatus>(e)
        }

    }

    override fun beforeExecute() {
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!destroyingFavoriteIds.contains(hashCode)) {
            destroyingFavoriteIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun afterExecute(callback: Any?, result: SingleResponse<ParcelableStatus>) {
        destroyingFavoriteIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        val taskEvent = FavoriteTaskEvent(FavoriteTaskEvent.Action.DESTROY,
                accountKey, statusId)
        taskEvent.isFinished = true
        if (result.hasData()) {
            val status = result.data
            taskEvent.status = status
            taskEvent.isSucceeded = true
            Utils.showInfoMessage(context, R.string.message_toast_status_unfavorited, false)
        } else {
            taskEvent.isSucceeded = false
            Utils.showErrorMessage(context, R.string.action_unfavoriting, result.exception, true)
        }
        bus.post(taskEvent)
        bus.post(StatusListChangedEvent())
    }

    companion object {
        private val destroyingFavoriteIds = ArrayIntList()

        fun isDestroyingFavorite(accountKey: UserKey?, statusId: String?): Boolean {
            return destroyingFavoriteIds.contains(calculateHashCode(accountKey, statusId))
        }

    }
}