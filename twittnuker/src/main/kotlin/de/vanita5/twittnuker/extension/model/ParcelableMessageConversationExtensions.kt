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

package de.vanita5.twittnuker.extension.model

import android.content.Context
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableMessage
import de.vanita5.twittnuker.model.ParcelableMessage.MessageType
import de.vanita5.twittnuker.model.ParcelableMessageConversation
import de.vanita5.twittnuker.model.ParcelableMessageConversation.ConversationType
import de.vanita5.twittnuker.model.ParcelableUser

fun ParcelableMessageConversation.setFrom(message: ParcelableMessage, details: AccountDetails) {
    account_key = details.key
    account_color = details.color
    message_type = message.message_type
    message_timestamp = message.message_timestamp
    local_timestamp = message.local_timestamp
    sort_id = message.sort_id
    text_unescaped = message.text_unescaped
    media = message.media
    spans = message.spans
    extras = message.extras
    sender_key = message.sender_key
    recipient_key = message.recipient_key
    is_outgoing = message.is_outgoing
    request_cursor = message.request_cursor
}

val ParcelableMessageConversation.timestamp: Long
    get() = if (message_timestamp > 0) message_timestamp else local_timestamp

fun ParcelableMessageConversation.getConversationName(context: Context): Pair<String, String?> {
    if (conversation_type == ConversationType.ONE_TO_ONE) {
        val user = this.user ?: return Pair(context.getString(R.string.direct_messages), null)
        return Pair(user.name, user.screen_name)
    }
    return Pair(participants.joinToString(separator = ", ") {
        it.name
    }, null)
}

fun ParcelableMessageConversation.getSummaryText(context: Context): String {
    when (message_type) {
        MessageType.STICKER -> {
            return context.getString(R.string.message_summary_type_sticker)
        }
    }
    return text_unescaped
}

val ParcelableMessageConversation.user: ParcelableUser?
    get() {
        val userKey = if (is_outgoing) recipient_key else sender_key
        return participants.firstOrNull { it.key == userKey }
    }