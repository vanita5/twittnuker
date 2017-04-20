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
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.User
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.event.FriendshipTaskEvent
import de.vanita5.twittnuker.util.Utils

class AcceptFriendshipTask(context: Context) : AbsFriendshipOperationTask(context, FriendshipTaskEvent.Action.ACCEPT) {

    @Throws(MicroBlogException::class)
    override fun perform(twitter: MicroBlog, details: AccountDetails, args: AbsFriendshipOperationTask.Arguments): User {
        when (details.type) {
            AccountType.FANFOU -> {
                return twitter.acceptFanfouFriendship(args.userKey.id)
            }
        }
        return twitter.acceptFriendship(args.userKey.id)
    }

    override fun succeededWorker(twitter: MicroBlog, details: AccountDetails, args: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        Utils.setLastSeen(context, user.key, System.currentTimeMillis())
    }

    override fun showSucceededMessage(params: AbsFriendshipOperationTask.Arguments, user: ParcelableUser) {
        val nameFirst = kPreferences[nameFirstKey]
        Toast.makeText(context, context.getString(R.string.message_toast_accepted_users_follow_request,
                manager.getDisplayName(user, nameFirst)), Toast.LENGTH_SHORT).show()
    }

}