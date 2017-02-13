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
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.KEY_NAME_FIRST
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.SingleResponse
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.UserListMembersChangedEvent
import de.vanita5.twittnuker.model.util.ParcelableUserListUtils
import de.vanita5.twittnuker.util.MicroBlogAPIFactory
import de.vanita5.twittnuker.util.Utils

class AddUserListMembersTask(
        context: Context,
        private val accountKey: UserKey,
        private val listId: String,
        private val users: Array<ParcelableUser>
) : ManagedAsyncTask<Any, Any, SingleResponse<ParcelableUserList>>(context) {

    override fun doInBackground(vararg params: Any): SingleResponse<ParcelableUserList> {
        val microBlog = MicroBlogAPIFactory.getInstance(context, accountKey) ?: return SingleResponse.getInstance<ParcelableUserList>()
        try {
            val userIds = users.map(ParcelableUser::key).toTypedArray()
            val result = microBlog.addUserListMembers(listId, UserKey.getIds(userIds))
            val list = ParcelableUserListUtils.from(result, accountKey)
            return SingleResponse.getInstance(list)
        } catch (e: MicroBlogException) {
            return SingleResponse.getInstance<ParcelableUserList>(e)
        }

    }

    override fun onPostExecute(result: SingleResponse<ParcelableUserList>) {
        if (result.data != null) {
            val message: String
            if (users.size == 1) {
                val user = users.first()
                val nameFirst = preferences.getBoolean(KEY_NAME_FIRST)
                val displayName = userColorNameManager.getDisplayName(user.key, user.name,
                        user.screen_name, nameFirst)
                message = context.getString(R.string.added_user_to_list, displayName, result.data.name)
            } else {
                val res = context.resources
                message = res.getQuantityString(R.plurals.added_N_users_to_list, users.size, users.size,
                        result.data.name)
            }
            Utils.showOkMessage(context, message, false)
            bus.post(UserListMembersChangedEvent(UserListMembersChangedEvent.Action.ADDED, result.data,
                    users))
        } else {
            Utils.showErrorMessage(context, R.string.action_adding_member, result.exception, true)
        }
        super.onPostExecute(result)
    }

}