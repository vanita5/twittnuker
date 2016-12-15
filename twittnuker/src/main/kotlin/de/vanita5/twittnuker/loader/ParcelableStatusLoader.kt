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

package de.vanita5.twittnuker.loader

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.support.v4.content.AsyncTaskLoader

import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.ErrorInfo
import de.vanita5.twittnuker.constant.IntentConstants
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.SingleResponse
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.UserColorNameManager
import de.vanita5.twittnuker.util.Utils.findStatus
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper

import javax.inject.Inject

class ParcelableStatusLoader(
        context: Context,
        private val omitIntentExtra: Boolean,
        private val extras: Bundle?,
        private val accountKey: UserKey?,
        private val statusId: String?
) : AsyncTaskLoader<SingleResponse<ParcelableStatus>>(context) {

    @Inject
    lateinit var userColorNameManager: UserColorNameManager

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun loadInBackground(): SingleResponse<ParcelableStatus> {
        if (accountKey == null || statusId == null) return SingleResponse.getInstance<ParcelableStatus>()
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true)
        if (!omitIntentExtra && extras != null) {
            val cache = extras.getParcelable<ParcelableStatus>(IntentConstants.EXTRA_STATUS)
            if (cache != null) {
                val response = SingleResponse.getInstance(cache)
                val extras = response.extras
                extras.putParcelable(EXTRA_ACCOUNT, details)
                return response
            }
        }
        try {
            if (details == null) return SingleResponse.getInstance<ParcelableStatus>()
            val status = findStatus(context, accountKey, statusId)
            ParcelableStatusUtils.updateExtraInformation(status, details, userColorNameManager)
            val response = SingleResponse.getInstance(status)
            val extras = response.extras
            extras.putParcelable(EXTRA_ACCOUNT, details)
            return response
        } catch (e: MicroBlogException) {
            if (e.errorCode == ErrorInfo.STATUS_NOT_FOUND) {
                // Delete all deleted status
                val cr = context.contentResolver
                DataStoreUtils.deleteStatus(cr, accountKey,
                        statusId, null)
                DataStoreUtils.deleteActivityStatus(cr, accountKey, statusId, null)
            }
            return SingleResponse.getInstance<ParcelableStatus>(e)
        }

    }

    override fun onStartLoading() {
        forceLoad()
    }

}