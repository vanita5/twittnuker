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
import android.support.v7.widget.RecyclerViewAccessor
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.bumptech.glide.RequestManager
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import de.vanita5.twittnuker.view.holder.AccountViewHolder

class AccountDetailsAdapter(
        context: Context,
        requestManager: RequestManager
) : BaseArrayAdapter<AccountDetails>(context, R.layout.list_item_account, requestManager = requestManager) {

    var sortEnabled: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var switchEnabled: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var accountToggleListener: ((Int, Boolean) -> Unit)? = null

    val checkedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        val position = buttonView.tag as? Int ?: return@OnCheckedChangeListener
        accountToggleListener?.invoke(position, isChecked)
    }

    init {
        GeneralComponentHelper.build(context).inject(this)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = super.getView(position, convertView, parent)
        val holder = view.tag as? AccountViewHolder ?: run {
            val h = AccountViewHolder(this, view)
            view.tag = h
            return@run h
        }
        RecyclerViewAccessor.setLayoutPosition(holder, position)
        val details = getItem(position)
        holder.display(details)
        return view
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).key.hashCode().toLong()
    }

    fun drop(from: Int, to: Int) {
        val fromItem = getItem(from)
        removeAt(from)
        insert(fromItem, to)
    }

    fun findItem(key: UserKey): AccountDetails? {
        (0 until count).forEach { i ->
            val item = getItem(i)
            if (key == item.key) return item
        }
        return null
    }

}