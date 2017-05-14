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

package de.vanita5.twittnuker.view.holder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_simple_user_list.view.*
import org.mariotaku.ktextension.spannable
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.iface.IUserListsAdapter
import de.vanita5.twittnuker.extension.loadProfileImage
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.view.ProfileImageView

class SimpleUserListViewHolder(
        val adapter: IUserListsAdapter<*>,
        itemView: View
) : RecyclerView.ViewHolder(itemView) {

    val createdByView: TextView = itemView.createdBy
    val nameView: TextView = itemView.name
    val profileImageView: ProfileImageView = itemView.profileImage

    init {
        profileImageView.style = adapter.profileImageStyle
    }

    fun display(userList: ParcelableUserList) {
        nameView.spannable = userList.name
        createdByView.spannable = createdByView.context.getString(R.string.created_by,
                adapter.userColorNameManager.getDisplayName(userList, false))
        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            val context = itemView.context
            adapter.requestManager.loadProfileImage(context, userList, adapter.profileImageStyle,
                    profileImageView.cornerRadius, profileImageView.cornerRadiusRatio).into(profileImageView)
        } else {
            profileImageView.visibility = View.GONE
        }
    }
}