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

import android.accounts.AccountManager
import android.content.Context
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.User
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.FriendshipTaskEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.model.util.ParcelableUserUtils

abstract class AbsFriendshipOperationTask(
        context: Context,
        @FriendshipTaskEvent.Action protected val action: Int
) : ExceptionHandlingAbstractTask<AbsFriendshipOperationTask.Arguments, ParcelableUser,
        MicroBlogException, Any?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun beforeExecute() {
        microBlogWrapper.addUpdatingRelationshipId(params.accountKey, params.userKey)
        val event = FriendshipTaskEvent(action, params.accountKey,
                params.userKey)
        event.isFinished = false
        bus.post(event)
    }

    override fun afterExecute(callback: Any?, result: ParcelableUser?, exception: MicroBlogException?) {
        microBlogWrapper.removeUpdatingRelationshipId(params.accountKey, params.userKey)
        val event = FriendshipTaskEvent(action, params.accountKey,
                params.userKey)
        event.isFinished = true
        if (result != null) {
            val user = result
            showSucceededMessage(params, user)
            event.isSucceeded = true
            event.user = user
        } else {
            showErrorMessage(params, exception)
        }
        bus.post(event)
    }

    override fun onExecute(params: Arguments): ParcelableUser {
        val am = AccountManager.get(context)
        val details = AccountUtils.getAccountDetails(am, params.accountKey, true)
                ?: throw MicroBlogException("No account")
        val twitter = details.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val user = perform(twitter, details, params)
        val parcelableUser = ParcelableUserUtils.fromUser(user, params.accountKey)
        succeededWorker(twitter, details, params, parcelableUser)
        return parcelableUser
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