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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter.Companion.ITEM_VIEW_TYPE_LOAD_INDICATOR
import de.vanita5.twittnuker.adapter.iface.IUserListsAdapter
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.view.holder.LoadIndicatorViewHolder
import de.vanita5.twittnuker.view.holder.UserListViewHolder

class ParcelableUserListsAdapter(
        context: Context, requestManager: RequestManager
) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context, requestManager), IUserListsAdapter<List<ParcelableUserList>> {
    override val showAccountsColor: Boolean = false
    override val nameFirst: Boolean = preferences[nameFirstKey]
    override var userListClickListener: IUserListsAdapter.UserListClickListener? = null

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var data: List<ParcelableUserList>? = null


    fun getData(): List<ParcelableUserList>? {
        return data
    }


    override fun setData(data: List<ParcelableUserList>?): Boolean {
        this.data = data
        notifyDataSetChanged()
        return true
    }

    private fun bindUserList(holder: UserListViewHolder, position: Int) {
        holder.displayUserList(getUserList(position)!!)
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = userListsCount
        if (position and ILoadMoreSupportAdapter.START !== 0L) {
            count++
        }
        if (position and ILoadMoreSupportAdapter.END !== 0L) {
            count++
        }
        return count
    }

    override fun getUserList(position: Int): ParcelableUserList? {
        if (position == userListsCount) return null
        return data!![position]
    }

    override fun getUserListId(position: Int): String? {
        if (position == userListsCount) return null
        return data!![position].id
    }

    override val userListsCount: Int
        get() {
            if (data == null) return 0
            return data!!.size
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_USER_LIST -> {
                return createUserListViewHolder(this, inflater, parent)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_USER_LIST -> {
                bindUserList(holder as UserListViewHolder, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START !== 0L && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        if (position == userListsCount) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        return ITEM_VIEW_TYPE_USER_LIST
    }

    companion object {

        val ITEM_VIEW_TYPE_USER_LIST = 2

        fun createUserListViewHolder(adapter: IUserListsAdapter<*>,
                                     inflater: LayoutInflater,
                                     parent: ViewGroup): UserListViewHolder {
            val view = inflater.inflate(R.layout.list_item_user_list, parent, false)
            val holder = UserListViewHolder(view, adapter)
            holder.setOnClickListeners()
            holder.setupViewOptions()
            return holder
        }
    }


}