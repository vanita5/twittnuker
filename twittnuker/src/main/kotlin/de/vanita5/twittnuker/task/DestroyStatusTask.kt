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

package de.vanita5.twittnuker.task

import android.content.Context
import android.widget.Toast
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.twitter.model.ErrorInfo
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.extension.getErrorMessage
import de.vanita5.twittnuker.extension.model.api.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.StatusDestroyedEvent
import de.vanita5.twittnuker.model.event.StatusListChangedEvent
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.deleteActivityStatus

class DestroyStatusTask(
        context: Context,
        accountKey: UserKey,
        private val statusId: String
) : AbsAccountRequestTask<Any?, ParcelableStatus, Any?>(context, accountKey) {

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableStatus {
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        return microBlog.destroyStatus(statusId).toParcelable(account)
    }

    override fun onCleanup(account: AccountDetails, params: Any?, result: ParcelableStatus?, exception: MicroBlogException?) {
        if (result == null && exception?.errorCode != ErrorInfo.STATUS_NOT_FOUND) return
        DataStoreUtils.deleteStatus(context.contentResolver, account.key, statusId, result)
        context.contentResolver.deleteActivityStatus(account.key, statusId, result)
    }

    override fun beforeExecute() {
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!microBlogWrapper.destroyingStatusIds.contains(hashCode)) {
            microBlogWrapper.destroyingStatusIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun afterExecute(callback: Any?, result: ParcelableStatus?, exception: MicroBlogException?) {
        microBlogWrapper.destroyingStatusIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        if (result != null) {
            if (result.retweet_id != null) {
                Toast.makeText(context, R.string.message_toast_retweet_cancelled, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.message_toast_status_deleted, Toast.LENGTH_SHORT).show()
            }
            bus.post(StatusDestroyedEvent(result))
        } else {
            Toast.makeText(context, exception?.getErrorMessage(context), Toast.LENGTH_SHORT).show()
        }
    }

}