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

import android.content.Context
import android.widget.Toast
import org.apache.commons.collections.primitives.ArrayIntList
import org.mariotaku.kpreferences.get
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.mastodon.Mastodon
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.constant.TWITTER_ERROR_ALREADY_FAVORITED
import de.vanita5.twittnuker.constant.iWantMyStarsBackKey
import de.vanita5.twittnuker.extension.getErrorMessage
import de.vanita5.twittnuker.extension.model.api.mastodon.toParcelable
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.draft.StatusObjectActionExtras
import de.vanita5.twittnuker.model.event.FavoriteTaskEvent
import de.vanita5.twittnuker.model.event.StatusListChangedEvent
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.task.twitter.UpdateStatusTask
import de.vanita5.twittnuker.util.AsyncTwitterWrapper.Companion.calculateHashCode
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.updateStatusInfo

class CreateFavoriteTask(context: Context, accountKey: UserKey, private val status: ParcelableStatus) :
        AbsAccountRequestTask<Any?, ParcelableStatus, Any?>(context, accountKey) {

    private val statusId = status.id

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableStatus {
        val resolver = context.contentResolver
        val result = when (account.type) {
            AccountType.FANFOU -> {
                val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                microBlog.createFanfouFavorite(statusId).toParcelable(account)
            }
            AccountType.MASTODON -> {
                val mastodon = account.newMicroBlogInstance(context, cls = Mastodon::class.java)
                mastodon.favouriteStatus(statusId).toParcelable(account)
            }
            else -> {
                val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
                microBlog.createFavorite(statusId).toParcelable(account)
            }
        }
        Utils.setLastSeen(context, result.mentions, System.currentTimeMillis())

        resolver.updateStatusInfo(DataStoreUtils.STATUSES_ACTIVITIES_URIS, Statuses.COLUMNS,
                account.key, statusId, ParcelableStatus::class.java) { status ->
            if (result.id != status.id) return@updateStatusInfo status
            status.is_favorite = true
            status.reply_count = result.reply_count
            status.retweet_count = result.retweet_count
            status.favorite_count = result.favorite_count
            return@updateStatusInfo status
        }
        return result
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
            if (preferences[iWantMyStarsBackKey]) {
                Toast.makeText(context, R.string.message_toast_status_favorited,
                        Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.message_toast_status_liked,
                        Toast.LENGTH_SHORT).show()
            }
        } else {
            taskEvent.isSucceeded = false
            Toast.makeText(context, exception?.getErrorMessage(context), Toast.LENGTH_SHORT).show()
        }
        bus.post(taskEvent)
        bus.post(StatusListChangedEvent())
    }

    override fun onCleanup(account: AccountDetails, params: Any?, exception: MicroBlogException) {
        if (exception.errorCode == TWITTER_ERROR_ALREADY_FAVORITED) {
            val resolver = context.contentResolver

            resolver.updateStatusInfo(DataStoreUtils.STATUSES_ACTIVITIES_URIS, Statuses.COLUMNS,
                    account.key, statusId, ParcelableStatus::class.java) { status ->
                if (statusId != status.id) return@updateStatusInfo status
                status.is_favorite = true
                return@updateStatusInfo status
            }
        }
    }

    override fun createDraft() = UpdateStatusTask.createDraft(Draft.Action.FAVORITE) {
        account_keys = arrayOf(accountKey)
        action_extras = StatusObjectActionExtras().also { extras ->
            extras.status = this@CreateFavoriteTask.status
        }
    }

    override fun deleteDraftOnException(account: AccountDetails, params: Any?, exception: MicroBlogException): Boolean {
        return exception.errorCode == TWITTER_ERROR_ALREADY_FAVORITED
    }

    companion object {

        private val creatingFavoriteIds = ArrayIntList()

        fun isCreatingFavorite(accountKey: UserKey?, statusId: String?): Boolean {
            return creatingFavoriteIds.contains(calculateHashCode(accountKey, statusId))
        }
    }

}