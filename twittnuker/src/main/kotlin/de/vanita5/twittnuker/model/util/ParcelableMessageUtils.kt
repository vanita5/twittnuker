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

package de.vanita5.twittnuker.model.util

import de.vanita5.twittnuker.library.twitter.model.DirectMessage
import de.vanita5.twittnuker.model.ParcelableMessage
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.message.MessageExtras
import de.vanita5.twittnuker.model.message.StickerExtras
import de.vanita5.twittnuker.util.InternalTwitterContentUtils


object ParcelableMessageUtils {
    fun incomingMessage(accountKey: UserKey, message: DirectMessage): ParcelableMessage {
        val result = message(accountKey, message)
        result.is_outgoing = false
        result.conversation_id = "${message.recipientId}-${message.senderId}"
        return result
    }

    fun outgoingMessage(accountKey: UserKey, message: DirectMessage): ParcelableMessage {
        val result = message(accountKey, message)
        result.is_outgoing = true
        result.conversation_id = "${message.senderId}-${message.recipientId}"
        return result
    }

    private fun message(accountKey: UserKey, message: DirectMessage): ParcelableMessage {
        val result = ParcelableMessage()
        result.account_key = accountKey
        result.id = message.id
        result.sender_key = UserKeyUtils.fromUser(message.sender)
        result.recipient_key = UserKeyUtils.fromUser(message.recipient)
        result.message_timestamp = message.createdAt.time
        result.local_timestamp = result.message_timestamp

        val (type, extras) = typeAndExtras(accountKey, message)
        val (text, spans) = InternalTwitterContentUtils.formatDirectMessageText(message)
        result.message_type = type
        result.extras = extras
        result.text_unescaped = text
        result.spans = spans
        result.media = ParcelableMediaUtils.fromEntities(message)
        return result
    }

    private fun typeAndExtras(accountKey: UserKey, message: DirectMessage): Pair<String, MessageExtras?> {
        val singleUrl = message.urlEntities?.singleOrNull()
        if (singleUrl != null) {
            if (singleUrl.expandedUrl.startsWith("https://twitter.com/i/stickers/image/")) {
                return Pair(ParcelableMessage.MessageType.STICKER, StickerExtras(singleUrl.expandedUrl))
            }
        }
        return Pair(ParcelableMessage.MessageType.TEXT, null)
    }
}