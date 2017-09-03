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

package de.vanita5.twittnuker.view.holder

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.list_item_user_list.view.*
import org.mariotaku.ktextension.hideIfEmpty
import org.mariotaku.ktextension.spannable
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.iface.IUserListsAdapter
import de.vanita5.twittnuker.extension.loadProfileImage
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.view.ColorLabelRelativeLayout
import de.vanita5.twittnuker.view.ProfileImageView
import java.util.*

class UserListViewHolder(
        itemView: View,
        private val adapter: IUserListsAdapter<*>
) : ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    private val itemContent: ColorLabelRelativeLayout = itemView.itemContent
    private val profileImageView: ProfileImageView = itemView.profileImage
    private val nameView: TextView = itemView.name
    private val createdByView: TextView = itemView.createdBy
    private val descriptionView: TextView = itemView.description
    private val membersCountView: TextView = itemView.membersCount
    private val subscribersCountView: TextView = itemView.subscribersCount

    private var userListClickListener: IUserListsAdapter.UserListClickListener? = null

    init {
        profileImageView.style = adapter.profileImageStyle
    }

    fun display(userList: ParcelableUserList) {
        val context = itemView.context
        val manager = adapter.userColorNameManager

        itemContent.drawStart(manager.getUserColor(userList.user_key))
        nameView.spannable = userList.name
        val nameFirst = adapter.nameFirst
        val createdByDisplayName = manager.getDisplayName(userList, nameFirst)
        createdByView.spannable = context.getString(R.string.created_by, createdByDisplayName)

        if (adapter.profileImageEnabled) {
            profileImageView.visibility = View.VISIBLE
            adapter.requestManager.loadProfileImage(context, userList, adapter.profileImageStyle,
                    profileImageView.cornerRadius, profileImageView.cornerRadiusRatio).into(profileImageView)
        } else {
            profileImageView.visibility = View.GONE
        }
        descriptionView.spannable = userList.description
        descriptionView.hideIfEmpty()
        membersCountView.text = Utils.getLocalizedNumber(Locale.getDefault(), userList.members_count)
        subscribersCountView.text = Utils.getLocalizedNumber(Locale.getDefault(), userList.subscribers_count)
    }

    fun setOnClickListeners() {
        setUserListClickListener(adapter.userListClickListener)
    }

    override fun onClick(v: View) {
        val listener = userListClickListener ?: return
        when (v.id) {
            R.id.itemContent -> {
                listener.onUserListClick(this, layoutPosition)
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        val listener = userListClickListener ?: return false
        when (v.id) {
            R.id.itemContent -> {
                return listener.onUserListLongClick(this, layoutPosition)
            }
        }
        return false
    }

    fun setUserListClickListener(listener: IUserListsAdapter.UserListClickListener?) {
        userListClickListener = listener
        itemContent.setOnClickListener(this)
        itemContent.setOnLongClickListener(this)
    }

    fun setupViewOptions() {
        profileImageView.style = adapter.profileImageStyle
        setTextSize(adapter.textSize)
    }

    fun setTextSize(textSize: Float) {
        nameView.textSize = textSize
        createdByView.textSize = textSize * 0.75f
    }

}