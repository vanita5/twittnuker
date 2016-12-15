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
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.kpreferences.KPreferences
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.User
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.SingleResponse
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.message.FriendshipTaskEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.model.util.ParcelableUserUtils
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.SharedPreferencesWrapper
import de.vanita5.twittnuker.util.UserColorNameManager
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper

import javax.inject.Inject

abstract class AbsFriendshipOperationTask(
        protected val context: Context,
        @FriendshipTaskEvent.Action protected val action: Int
) : AbstractTask<AbsFriendshipOperationTask.Arguments, SingleResponse<ParcelableUser>, Any?>() {

    @Inject
    lateinit var bus: Bus
    @Inject
    lateinit var twitter: AsyncTwitterWrapper
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    @Inject
    lateinit var kPreferences: KPreferences
    @Inject
    lateinit var manager: UserColorNameManager

    init {
        GeneralComponentHelper.build(context).inject(this)
    }


    override fun beforeExecute() {
        twitter.addUpdatingRelationshipId(params.accountKey, params.userKey)
        val event = FriendshipTaskEvent(action, params.accountKey,
                params.userKey)
        event.isFinished = false
        bus.post(event)
    }

    override fun afterExecute(handler: Any?, result: SingleResponse<ParcelableUser>?) {
        twitter.removeUpdatingRelationshipId(params.accountKey, params.userKey)
        val event = FriendshipTaskEvent(action, params.accountKey,
                params.userKey)
        event.isFinished = true
        if (result!!.hasData()) {
            val user = result.data!!
            showSucceededMessage(params, user)
            event.isSucceeded = true
            event.user = result.data
        } else {
            showErrorMessage(params, result.exception)
        }
        bus.post(event)
    }

    public override fun doLongOperation(args: Arguments): SingleResponse<ParcelableUser> {
        val details = AccountUtils.getAccountDetails(AccountManager.get(context), args.accountKey, true) ?: return SingleResponse.getInstance<ParcelableUser>()
        val twitter = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        try {
            val user = perform(twitter, details, args)
            val parcelableUser = ParcelableUserUtils.fromUser(user, args.accountKey)
            succeededWorker(twitter, details, args, parcelableUser)
            return SingleResponse.getInstance(parcelableUser)
        } catch (e: MicroBlogException) {
            return SingleResponse.getInstance<ParcelableUser>(e)
        }

    }

    @Throws(MicroBlogException::class)
    protected abstract fun perform(twitter: MicroBlog,
                                   details: AccountDetails,
                                   args: Arguments): User

    protected abstract fun succeededWorker(twitter: MicroBlog,
                                           details: AccountDetails,
                                           args: Arguments,
                                           user: ParcelableUser)

    protected abstract fun showSucceededMessage(params: Arguments, user: ParcelableUser)

    protected abstract fun showErrorMessage(params: Arguments, exception: Exception?)

    fun setup(accountKey: UserKey, userKey: UserKey) {
        params = Arguments(accountKey, userKey)
    }

    class Arguments(val accountKey: UserKey, val userKey: UserKey)

}