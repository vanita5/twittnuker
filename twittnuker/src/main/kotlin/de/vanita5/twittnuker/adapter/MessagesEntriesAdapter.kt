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

package de.vanita5.twittnuker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.vanita5.twittnuker.adapter.iface.IItemCountsAdapter
import de.vanita5.twittnuker.model.ItemCounts
import de.vanita5.twittnuker.model.ParcelableMessageConversation
import de.vanita5.twittnuker.view.holder.message.MessageEntryViewHolder


class MessagesEntriesAdapter(context: Context) : LoadMoreSupportAdapter<RecyclerView.ViewHolder>(context),
        IItemCountsAdapter {
    override val itemCounts: ItemCounts = ItemCounts(1)

    var conversations: List<ParcelableMessageConversation>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var drawAccountColors: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var listener: MessageConversationClickListener? = null

    override fun getItemCount(): Int {
        itemCounts[0] = conversations?.size ?: 0
        return itemCounts.itemCount
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_TYPE_MESSAGE_ENTRY -> {
                val conversation = getConversation(position)!!
                (holder as MessageEntryViewHolder).display(conversation)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(MessageEntryViewHolder.layoutResource, parent, false)
        return MessageEntryViewHolder(itemView, this)
    }

    override fun getItemViewType(position: Int): Int {
        return ITEM_TYPE_MESSAGE_ENTRY
    }

    fun getConversation(position: Int): ParcelableMessageConversation? {
        return conversations?.get(position - itemCounts.getItemStartPosition(0))
    }

    interface MessageConversationClickListener {
        fun onProfileImageClick(position: Int)
        fun onConversationClick(position: Int)

    }

    companion object {
        const val ITEM_TYPE_MESSAGE_ENTRY = 1

    }

}