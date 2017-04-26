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

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView

import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.AccountDetailsAdapter
import de.vanita5.twittnuker.extension.loadProfileImage
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.view.ProfileImageView
import de.vanita5.twittnuker.view.iface.IColorLabelView

class AccountViewHolder(
        val adapter: AccountDetailsAdapter,
        itemView: View
) : RecyclerView.ViewHolder(itemView) {

    private val content = itemView as IColorLabelView
    private val name = itemView.findViewById(android.R.id.text1) as TextView
    private val screenName = itemView.findViewById(android.R.id.text2) as TextView
    private val profileImage = itemView.findViewById(android.R.id.icon) as ProfileImageView
    private val toggle = itemView.findViewById(android.R.id.toggle) as CompoundButton
    private val toggleContainer = itemView.findViewById(R.id.toggle_container)
    private val accountType = itemView.findViewById(R.id.account_type) as ImageView
    private val dragHandle = itemView.findViewById(R.id.drag_handle)

    init {
        profileImage.style = adapter.profileImageStyle
    }

    fun setAccountColor(color: Int) {
        content.drawEnd(color)
    }

    fun setSortEnabled(enabled: Boolean) {
        dragHandle.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    @SuppressLint("SetTextI18n")
    fun display(details: AccountDetails) {
        name.text = details.user.name
        screenName.text = "@${details.user.screen_name}"
        setAccountColor(details.color)
        profileImage.visibility = View.VISIBLE
        adapter.requestManager.loadProfileImage(adapter.context, details, adapter.profileImageStyle,
                profileImage.cornerRadius, profileImage.cornerRadiusRatio).into(profileImage)
        accountType.setImageResource(AccountUtils.getAccountTypeIcon(details.type))
        toggle.isChecked = details.activated
        toggle.setOnCheckedChangeListener(adapter.checkedChangeListener)
        toggle.tag = layoutPosition
        toggleContainer.visibility = if (adapter.switchEnabled) View.VISIBLE else View.GONE
        setSortEnabled(adapter.sortEnabled)
    }
}