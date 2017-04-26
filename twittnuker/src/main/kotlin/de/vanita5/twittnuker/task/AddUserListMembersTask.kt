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
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.mapToArray
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.extension.model.api.microblog.toParcelable
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.UserListMembersChangedEvent

class AddUserListMembersTask(
        context: Context,
        accountKey: UserKey,
        private val listId: String,
        private val users: Array<out ParcelableUser>
) : AbsAccountRequestTask<Any?, ParcelableUserList, Any?>(context, accountKey) {

    override fun onExecute(account: AccountDetails, params: Any?): ParcelableUserList {
        val microBlog = account.newMicroBlogInstance(context, MicroBlog::class.java)
        val userIds = users.mapToArray(ParcelableUser::key)
        val result = microBlog.addUserListMembers(listId, UserKey.getIds(userIds))
        return result.toParcelable(account.key)
    }

    override fun onSucceed(callback: Any?, result: ParcelableUserList) {
        val message: String
        if (users.size == 1) {
            val user = users.first()
            val nameFirst = preferences[nameFirstKey]
            val displayName = userColorNameManager.getDisplayName(user.key, user.name,
                    user.screen_name, nameFirst)
            message = context.getString(R.string.message_toast_added_user_to_list, displayName, result.name)
        } else {
            val res = context.resources
            message = res.getQuantityString(R.plurals.added_N_users_to_list, users.size, users.size,
                    result.name)
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        bus.post(UserListMembersChangedEvent(UserListMembersChangedEvent.Action.ADDED, result, users))
    }

}