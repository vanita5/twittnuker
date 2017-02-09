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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.database.Cursor
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.iface.IContentAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter
import de.vanita5.twittnuker.annotation.CustomTabType
import de.vanita5.twittnuker.constant.mediaPreviewStyleKey
import de.vanita5.twittnuker.model.StringLongPair
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore.DirectMessages.ConversationEntries
import de.vanita5.twittnuker.util.ReadStateManager.OnReadStateChangeListener
import de.vanita5.twittnuker.view.holder.LoadIndicatorViewHolder
import de.vanita5.twittnuker.view.holder.MessageEntryViewHolder

class MessageEntriesAdapter(context: Context) : LoadMoreSupportAdapter<ViewHolder>(context),
        IContentAdapter, OnReadStateChangeListener {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val mediaPreviewStyle: Int

    private val readStateChangeListener: OnSharedPreferenceChangeListener
    private var showAccountsColor: Boolean = false
    private var cursor: Cursor? = null
    var listener: MessageEntriesAdapterListener? = null
    private var positionPairs: Array<StringLongPair>? = null

    init {
        mediaPreviewStyle = preferences[mediaPreviewStyleKey]
        readStateChangeListener = OnSharedPreferenceChangeListener { sharedPreferences, key -> updateReadState() }
    }


    fun getEntry(position: Int): DirectMessageEntry? {
        val c = cursor
        if (c == null || c.isClosed || !c.moveToPosition(position)) return null
        return DirectMessageEntry(c)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_MESSAGE -> {
                val view = inflater.inflate(R.layout.list_item_message_entry, parent, false)
                return MessageEntryViewHolder(this, view)
            }
            ITEM_VIEW_TYPE_LOAD_INDICATOR -> {
                val view = inflater.inflate(R.layout.list_item_load_indicator, parent, false)
                return LoadIndicatorViewHolder(view)
            }
        }
        throw IllegalStateException("Unknown view type " + viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_MESSAGE -> {
                val c = cursor
                c!!.moveToPosition(position)
                (holder as MessageEntryViewHolder).displayMessage(c, isUnread(c))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (loadMoreIndicatorPosition and ILoadMoreSupportAdapter.START !== 0L && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        if (position == messagesCount) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR
        }
        return ITEM_VIEW_TYPE_MESSAGE
    }

    override fun getItemCount(): Int {
        val position = loadMoreIndicatorPosition
        var count = messagesCount
        if (position and ILoadMoreSupportAdapter.START !== 0L) {
            count++
        }
        if (position and ILoadMoreSupportAdapter.END !== 0L) {
            count++
        }
        return count
    }

    fun onMessageClick(position: Int) {
        if (listener == null) return
        listener!!.onEntryClick(position, getEntry(position)!!)
    }

    override fun onReadStateChanged() {

    }

    fun onUserProfileClick(position: Int) {
        listener!!.onUserClick(position, getEntry(position)!!)
    }

    fun setCursor(cursor: Cursor?) {
        this.cursor = cursor
        readStateManager.unregisterOnSharedPreferenceChangeListener(readStateChangeListener)
        if (cursor != null) {
            updateReadState()
            readStateManager.registerOnSharedPreferenceChangeListener(readStateChangeListener)
        }
        notifyDataSetChanged()
    }


    fun updateReadState() {
        positionPairs = readStateManager.getPositionPairs(CustomTabType.DIRECT_MESSAGES)
        notifyDataSetChanged()
    }

    private val messagesCount: Int
        get() {
            val c = cursor
            if (c == null || c.isClosed) return 0
            return c.count
        }

    private fun isUnread(c: Cursor): Boolean {
        val positionPairs = this.positionPairs ?: return true
        val accountId = c.getLong(ConversationEntries.IDX_ACCOUNT_KEY)
        val conversationId = c.getLong(ConversationEntries.IDX_CONVERSATION_ID)
        val messageId = c.getLong(ConversationEntries.IDX_MESSAGE_ID)
        val key = "$accountId-$conversationId"
        val match = positionPairs.find { key == it.key } ?: return true
        return messageId > match.value
    }

    fun setShowAccountsColor(showAccountsColor: Boolean) {
        if (this.showAccountsColor == showAccountsColor) return
        this.showAccountsColor = showAccountsColor
        notifyDataSetChanged()
    }

    fun shouldShowAccountsColor(): Boolean {
        return showAccountsColor
    }

    interface MessageEntriesAdapterListener {
        fun onEntryClick(position: Int, entry: DirectMessageEntry)

        fun onUserClick(position: Int, entry: DirectMessageEntry)
    }

    class DirectMessageEntry internal constructor(cursor: Cursor) {

        val account_key: UserKey
        val conversation_id: String
        val screen_name: String
        val name: String

        init {
            account_key = UserKey.valueOf(cursor.getString(ConversationEntries.IDX_ACCOUNT_KEY))
            conversation_id = cursor.getString(ConversationEntries.IDX_CONVERSATION_ID)
            screen_name = cursor.getString(ConversationEntries.IDX_SCREEN_NAME)
            name = cursor.getString(ConversationEntries.IDX_NAME)
        }

    }

    companion object {

        val ITEM_VIEW_TYPE_MESSAGE = 0
        val ITEM_VIEW_TYPE_LOAD_INDICATOR = 1
    }

}