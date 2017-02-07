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

package de.vanita5.twittnuker.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.util.view.display
import de.vanita5.twittnuker.view.holder.SimpleUserListViewHolder

class SimpleParcelableUserListsAdapter(
        context: Context
) : BaseArrayAdapter<ParcelableUserList>(context, R.layout.list_item_simple_user_list) {

    override fun getItemId(position: Int): Long {
        return (if (getItem(position) != null) getItem(position).hashCode() else -1).toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val tag = view.tag
        val holder = tag as? SimpleUserListViewHolder ?: run {
            val h = SimpleUserListViewHolder(view)
            view.tag = h
            return@run h
        }
        val userList = getItem(position)
        holder.display(userList, mediaLoader, userColorNameManager, profileImageEnabled)
        return view
    }

    fun setData(data: List<ParcelableUserList>?) {
        clear()
        if (data == null) return
        for (user in data) {
            //TODO improve compare
            if (findItemPosition(user.hashCode().toLong()) < 0) {
                add(user)
            }
        }
    }

}