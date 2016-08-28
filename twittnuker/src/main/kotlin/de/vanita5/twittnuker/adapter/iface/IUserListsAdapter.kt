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

package de.vanita5.twittnuker.adapter.iface

import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.view.holder.UserListViewHolder

interface IUserListsAdapter<Data> : IContentCardAdapter {

    val userListsCount: Int

    val nameFirst: Boolean

    val showAccountsColor: Boolean

    val userListClickListener: UserListClickListener?

    fun getUserList(position: Int): ParcelableUserList?

    fun getUserListId(position: Int): String?

    fun setData(data: Data?): Boolean

    interface UserListClickListener {

        fun onUserListClick(holder: UserListViewHolder, position: Int)

        fun onUserListLongClick(holder: UserListViewHolder, position: Int): Boolean

    }
}