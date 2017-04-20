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

import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import org.apache.commons.collections.primitives.ArrayIntList
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.getErrorMessage
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.draft.StatusObjectActionExtras
import de.vanita5.twittnuker.model.event.FavoriteTaskEvent
import de.vanita5.twittnuker.model.event.StatusListChangedEvent
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils
import de.vanita5.twittnuker.provider.TwidereDataStore
import de.vanita5.twittnuker.task.twitter.UpdateStatusTask
import de.vanita5.twittnuker.util.AsyncTwitterWrapper.Companion.calculateHashCode
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.updateActivityStatus

class CreateFavoriteTask(context: Context, accountKey: UserKey, private val status: ParcelableStatus) :
        AbsAccountRequestTask<Any?, ParcelableStatus, Any?>(context, accountKey) {

    private val statusId = status.id
    override fun onExecute(account: AccountDetails, params: Any?): ParcelableStatus {
        val draftId = UpdateStatusTask.saveDraft(context, Draft.Action.FAVORITE) {
            this@saveDraft.account_keys = arrayOf(accountKey)
            this@saveDraft.action_extras = StatusObjectActionExtras().apply {
                this@apply.status = this@CreateFavoriteTask.status
            }
        }
        microBlogWrapper.addSendingDraftId(draftId)
        val resolver = context.contentResolver
        try {
            val result = when (account.type) {
                AccountType.FANFOU -> {
                    val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                    microBlog.createFanfouFavorite(statusId).toParcelable(account.key, account.type)
                }
                else -> {
                    val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                    microBlog.createFavorite(statusId).toParcelable(account.key, account.type)
                }
            }
            ParcelableStatusUtils.updateExtraInformation(result, account)
            Utils.setLastSeen(context, result.mentions, System.currentTimeMillis())
            val values = ContentValues()
            values.put(TwidereDataStore.Statuses.IS_FAVORITE, true)
            values.put(TwidereDataStore.Statuses.REPLY_COUNT, result.reply_count)
            values.put(TwidereDataStore.Statuses.RETWEET_COUNT, result.retweet_count)
            values.put(TwidereDataStore.Statuses.FAVORITE_COUNT, result.favorite_count)
            val statusWhere = Expression.and(
                    Expression.equalsArgs(TwidereDataStore.Statuses.ACCOUNT_KEY),
                    Expression.or(
                            Expression.equalsArgs(TwidereDataStore.Statuses.STATUS_ID),
                            Expression.equalsArgs(TwidereDataStore.Statuses.RETWEET_ID)
                    )
            ).sql
            val statusWhereArgs = arrayOf(account.key.toString(), statusId, statusId)
            for (uri in DataStoreUtils.STATUSES_URIS) {
                resolver.update(uri, values, statusWhere, statusWhereArgs)
            }
            resolver.updateActivityStatus(account.key, statusId) { activity ->
                val statusesMatrix = arrayOf(activity.target_statuses, activity.target_object_statuses)
                for (statusesArray in statusesMatrix) {
                    if (statusesArray == null) continue
                    for (status in statusesArray) {
                        if (result.id != status.id) continue
                        status.is_favorite = true
                        status.reply_count = result.reply_count
                        status.retweet_count = result.retweet_count
                        status.favorite_count = result.favorite_count
                    }
                }
            }
            UpdateStatusTask.deleteDraft(context, draftId)
            return result
        } finally {
            microBlogWrapper.removeSendingDraftId(draftId)
        }
    }

    override fun beforeExecute() {
        val hashCode = calculateHashCode(accountKey, statusId)
        if (!creatingFavoriteIds.contains(hashCode)) {
            creatingFavoriteIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun afterExecute(callback: Any?, result: ParcelableStatus?, exception: MicroBlogException?) {
        creatingFavoriteIds.removeElement(calculateHashCode(accountKey, statusId))
        val taskEvent = FavoriteTaskEvent(FavoriteTaskEvent.Action.CREATE, accountKey, statusId)
        taskEvent.isFinished = true
        if (result != null) {
            taskEvent.status = result
            taskEvent.isSucceeded = true
        } else {
            taskEvent.isSucceeded = false
            Toast.makeText(context, exception?.getErrorMessage(context), Toast.LENGTH_SHORT).show()
        }
        bus.post(taskEvent)
        bus.post(StatusListChangedEvent())
    }


    companion object {

        private val creatingFavoriteIds = ArrayIntList()

        fun isCreatingFavorite(accountKey: UserKey?, statusId: String?): Boolean {
            return creatingFavoriteIds.contains(calculateHashCode(accountKey, statusId))
        }
    }

}