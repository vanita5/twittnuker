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

package de.vanita5.twittnuker.task

import android.accounts.AccountManager
import android.content.Context
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.ErrorInfo
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.SingleResponse
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.message.StatusDestroyedEvent
import de.vanita5.twittnuker.model.message.StatusListChangedEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.Utils

class DestroyStatusTask(
        context: Context,
        private val accountKey: UserKey,
        private val statusId: String
) : ManagedAsyncTask<Any, Any, SingleResponse<ParcelableStatus>>(context) {

    override fun doInBackground(vararg params: Any): SingleResponse<ParcelableStatus> {
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
                ?: return SingleResponse()
        val microBlog = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        var status: ParcelableStatus? = null
        var deleteStatus: Boolean = false
        try {
            status = ParcelableStatusUtils.fromStatus(microBlog.destroyStatus(statusId),
                    accountKey, false)
            ParcelableStatusUtils.updateExtraInformation(status, details)
            deleteStatus = true
            return SingleResponse(status)
        } catch (e: MicroBlogException) {
            deleteStatus = e.errorCode == ErrorInfo.STATUS_NOT_FOUND
            return SingleResponse(exception = e)
        } finally {
            if (deleteStatus) {
                DataStoreUtils.deleteStatus(context.contentResolver, accountKey, statusId, status)
                DataStoreUtils.deleteActivityStatus(context.contentResolver, accountKey, statusId, status)
            }
        }
    }

    override fun onPreExecute() {
        super.onPreExecute()
        val hashCode = AsyncTwitterWrapper.calculateHashCode(accountKey, statusId)
        if (!asyncTwitterWrapper.destroyingStatusIds.contains(hashCode)) {
            asyncTwitterWrapper.destroyingStatusIds.add(hashCode)
        }
        bus.post(StatusListChangedEvent())
    }

    override fun onPostExecute(result: SingleResponse<ParcelableStatus>) {
        asyncTwitterWrapper.destroyingStatusIds.removeElement(AsyncTwitterWrapper.calculateHashCode(accountKey, statusId))
        if (result.hasData()) {
            val status = result.data!!
            if (status.retweet_id != null) {
                Utils.showInfoMessage(context, R.string.message_retweet_cancelled, false)
            } else {
                Utils.showInfoMessage(context, R.string.message_status_deleted, false)
            }
            bus.post(StatusDestroyedEvent(status))
        } else {
            Utils.showErrorMessage(context, R.string.action_deleting, result.exception, true)
        }
        super.onPostExecute(result)
    }

}