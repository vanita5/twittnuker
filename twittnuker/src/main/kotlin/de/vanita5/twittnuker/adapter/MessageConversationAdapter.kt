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

package de.vanita5.twittnuker.adapter

import android.content.Context
import android.database.Cursor
import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.iface.IDirectMessagesAdapter
import de.vanita5.twittnuker.constant.*
import de.vanita5.twittnuker.model.ParcelableDirectMessage
import de.vanita5.twittnuker.model.ParcelableDirectMessageCursorIndices
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.DirectMessageOnLinkClickHandler
import de.vanita5.twittnuker.util.IntentUtils
import de.vanita5.twittnuker.util.MediaLoadingHandler
import de.vanita5.twittnuker.util.ThemeUtils
import de.vanita5.twittnuker.util.TwidereLinkify
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.view.CardMediaContainer
import de.vanita5.twittnuker.view.holder.IncomingMessageViewHolder
import de.vanita5.twittnuker.view.holder.MessageViewHolder

import java.lang.ref.WeakReference

class MessageConversationAdapter(context: Context) : BaseRecyclerViewAdapter<ViewHolder>(context), Constants, IDirectMessagesAdapter {
    private val outgoingMessageColor: Int = ThemeUtils.getCardBackgroundColor(context,
            ThemeUtils.getThemeBackgroundOption(context), ThemeUtils.getUserThemeBackgroundAlpha(context))
    private val incomingMessageColor: Int = ThemeUtils.getUserAccentColor(context)

    override val mediaPreviewStyle: Int = Utils.getMediaPreviewStyle(preferences.getString(SharedPreferenceConstants.KEY_MEDIA_PREVIEW_STYLE, null))

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    val mediaLoadingHandler: MediaLoadingHandler = MediaLoadingHandler(R.id.media_preview_progress)

    val linkify: TwidereLinkify = TwidereLinkify(DirectMessageOnLinkClickHandler(context, null, preferences))
    val onMediaClickListener: CardMediaContainer.OnMediaClickListener

    private var cursor: Cursor? = null
    private var indices: ParcelableDirectMessageCursorIndices? = null

    init {
        onMediaClickListener = EventListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when (viewType) {
            ITEM_VIEW_TYPE_MESSAGE_INCOMING -> {
                val view = inflater.inflate(R.layout.card_item_message_conversation_incoming, parent, false)
                val holder = IncomingMessageViewHolder(this, view)
                holder.setMessageColor(incomingMessageColor)
                holder.setTextSize(textSize)
                return holder
            }
            ITEM_VIEW_TYPE_MESSAGE_OUTGOING -> {
                val view = inflater.inflate(R.layout.card_item_message_conversation_outgoing, parent, false)
                val holder = MessageViewHolder(this, view)
                holder.setMessageColor(outgoingMessageColor)
                holder.setTextSize(textSize)
                return holder
            }
        }
        throw UnsupportedOperationException("Unknown viewType " + viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ITEM_VIEW_TYPE_MESSAGE_INCOMING, ITEM_VIEW_TYPE_MESSAGE_OUTGOING -> {
                val c = cursor
                c!!.moveToPosition(getCursorPosition(position))
                (holder as MessageViewHolder).displayMessage(c, indices!!)
            }
        }
    }

    private fun getCursorPosition(position: Int): Int {
        return position
    }

    override fun getItemViewType(position: Int): Int {
        val c = cursor!!
        val i = indices!!
        c.moveToPosition(getCursorPosition(position))
        if (c.getInt(i.is_outgoing) == 1) {
            return ITEM_VIEW_TYPE_MESSAGE_OUTGOING
        } else {
            return ITEM_VIEW_TYPE_MESSAGE_INCOMING
        }
    }

    override fun getItemCount(): Int {
        val c = cursor ?: return 0
        return c.count
    }

    override fun findItem(id: Long): ParcelableDirectMessage? {
        for (i in 0 until itemCount) {
            if (getItemId(i) == id) return getDirectMessage(i)
        }
        return null
    }

    fun getDirectMessage(position: Int): ParcelableDirectMessage? {
        val c = cursor
        if (c == null || c.isClosed) return null
        c.moveToPosition(position)
        val accountKey = UserKey.valueOf(c.getString(indices!!.account_key))
        val messageId = c.getLong(indices!!.id)
        return Utils.findDirectMessageInDatabases(context, accountKey, messageId)
    }

    fun setCursor(cursor: Cursor?) {
        if (cursor != null) {
            indices = ParcelableDirectMessageCursorIndices(cursor)
        } else {
            indices = null
        }
        this.cursor = cursor
        notifyDataSetChanged()
    }


    internal class EventListener(adapter: MessageConversationAdapter) : CardMediaContainer.OnMediaClickListener {

        private val adapterRef: WeakReference<MessageConversationAdapter>

        init {
            this.adapterRef = WeakReference(adapter)
        }

        override fun onMediaClick(view: View, media: ParcelableMedia, accountKey: UserKey, extraId: Long) {
            val adapter = adapterRef.get()
            IntentUtils.openMedia(adapter.context, adapter.getDirectMessage(extraId.toInt())!!, media,
                    adapter.preferences[newDocumentApiKey], adapter.preferences[displaySensitiveContentsKey])
        }

    }

    companion object {

        private val ITEM_VIEW_TYPE_MESSAGE_OUTGOING = 1
        private val ITEM_VIEW_TYPE_MESSAGE_INCOMING = 2
    }
}