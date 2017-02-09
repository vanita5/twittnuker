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
import de.vanita5.twittnuker.TwittnukerConstants
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.SingleResponse
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.draft.StatusObjectExtras
import de.vanita5.twittnuker.model.message.StatusListChangedEvent
import de.vanita5.twittnuker.model.message.StatusRetweetedEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils
import de.vanita5.twittnuker.provider.TwidereDataStore
import de.vanita5.twittnuker.task.twitter.UpdateStatusTask
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.AsyncTwitterWrapper.calculateHashCode
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.Utils

class RetweetStatusTask(
        context: Context,
        private val accountKey: UserKey,
        private val status: ParcelableStatus
) : BaseAbstractTask<Any, SingleResponse<ParcelableStatus>, Any?>(context) {

    private val statusId = status.id

    override fun doLongOperation(params: Any?): SingleResponse<ParcelableStatus> {
        val draftId = UpdateStatusTask.saveDraft(context, Draft.Action.RETWEET) {
            this@saveDraft.account_keys = arrayOf(accountKey)
            this@saveDraft.action_extras = StatusObjectExtras().apply {
                this@apply.status = this@RetweetStatusTask.status
            }
        }
        microBlogWrapper.addSendingDraftId(draftId)
        val resolver = context.contentResolver
        val details = AccountUtils.getAccountDetails(AccountManager.get(context),
                accountKey, true) ?: return SingleResponse.getInstance<ParcelableStatus>(MicroBlogException("No account"))
        val microBlog = details.newMicroBlogInstance(
                context, false, false, null, MicroBlog::class.java)
        try {
            val result = ParcelableStatusUtils.fromStatus(microBlog.retweetStatus(statusId),
                    accountKey, false)
            ParcelableStatusUtils.updateExtraInformation(result, details)
            Utils.setLastSeen(context, result.mentions, System.currentTimeMillis())
            val values = ContentValues()
            values.put(TwidereDataStore.Statuses.MY_RETWEET_ID, result.id)
            values.put(TwidereDataStore.Statuses.REPLY_COUNT, result.reply_count)
            values.put(TwidereDataStore.Statuses.RETWEET_COUNT, result.retweet_count)
            values.put(TwidereDataStore.Statuses.FAVORITE_COUNT, result.favorite_count)
            val where = Expression.or(
                    Expression.equalsArgs(TwidereDataStore.Statuses.STATUS_ID),
                    Expression.equalsArgs(TwidereDataStore.Statuses.RETWEET_ID)
            )
            val whereArgs = arrayOf(statusId, statusId)
            for (uri in DataStoreUtils.STATUSES_URIS) {
                resolver.update(uri, values, where.sql, whereArgs)
            }
            DataStoreUtils.updateActivityStatus(resolver, accountKey, statusId, DataStoreUtils.UpdateActivityAction { activity ->
                val statusesMatrix = arrayOf(activity.target_statuses, activity.target_object_statuses)
                activity.status_my_retweet_id = result.my_retweet_id
                for (statusesArray in statusesMatrix) {
                    if (statusesArray == null) continue
                    for (status in statusesArray) {
                        if (statusId == status.id || statusId == status.retweet_id
                                || statusId == status.my_retweet_id) {
                            status.my_retweet_id = result.id
                            status.reply_count = result.reply_count
                            status.retweet_count = result.retweet_count
                            status.favorite_count = result.favorite_count
                        }
                    }
                }
            })
            UpdateStatusTask.deleteDraft(context, draftId)
            return SingleResponse(result)
        } catch (e: MicroBlogException) {
            DebugLog.w(TwittnukerConstants.LOGTAG, tr = e)
            return SingleResponse(e)
        } finally {
            microBlogWrapper.removeSendingDraftId(draftId)
        }

    }

    override fun beforeExecute() {
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!creatingRetweetIds.contains(hashCode)) {
            creatingRetweetIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }


    override fun afterExecute(callback: Any?, result: SingleResponse<ParcelableStatus>) {
        creatingRetweetIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        if (result.hasData()) {
            val status = result.data

            bus.post(StatusRetweetedEvent(status))
        } else {
            Utils.showErrorMessage(context, R.string.action_retweeting, result.exception, true)
        }
    }

    companion object {
        private val creatingRetweetIds = ArrayIntList()
        fun isCreatingRetweet(accountKey: UserKey?, statusId: String?): Boolean {
            return creatingRetweetIds.contains(calculateHashCode(accountKey, statusId))
        }

    }

}