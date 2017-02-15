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
import de.vanita5.twittnuker.model.ParcelableMessage
import de.vanita5.twittnuker.model.ParcelableMessage.MessageType
import de.vanita5.twittnuker.model.ParcelableMessageConversation
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.message.MessageExtras
import de.vanita5.twittnuker.model.message.NameUpdatedExtras
import de.vanita5.twittnuker.model.message.UserArrayExtras
import de.vanita5.twittnuker.util.UserColorNameManager


val ParcelableMessage.timestamp: Long
    get() = if (message_timestamp > 0) message_timestamp else local_timestamp

fun ParcelableMessage.getSummaryText(context: Context, manager: UserColorNameManager,
        conversation: ParcelableMessageConversation?, nameFirst: Boolean): CharSequence? {
    return getSummaryText(context, manager, nameFirst, message_type, extras, sender_key,
            text_unescaped, conversation)
}

internal fun getSummaryText(context: Context, manager: UserColorNameManager, nameFirst: Boolean,
        messageType: String?, extras: MessageExtras?, senderKey: UserKey?, text: String?,
        conversation: ParcelableMessageConversation?): CharSequence? {
    when (messageType) {
        MessageType.STICKER -> {
            return context.getString(R.string.message_summary_type_sticker)
        }
        MessageType.JOIN_CONVERSATION -> {
            return context.getString(R.string.message_join_conversation)
        }
        MessageType.CONVERSATION_CREATE -> {
            return context.getString(R.string.message_conversation_created)
        }
        MessageType.PARTICIPANTS_JOIN -> {
            val users = (extras as UserArrayExtras).users
            val sender = conversation?.participants?.firstOrNull { senderKey == it.key }
            val res = context.resources
            val joinName = if (users.size == 1) {
                manager.getDisplayName(users[0], nameFirst)
            } else {
                res.getQuantityString(R.plurals.N_users, users.size, users.size)
            }
            if (sender != null) {
                return res.getString(R.string.message_format_participants_join_added,
                        manager.getDisplayName(sender, nameFirst), joinName)
            } else {
                return res.getString(R.string.message_format_participants_join, joinName)
            }
        }
        MessageType.PARTICIPANTS_LEAVE -> {
            val users = (extras as UserArrayExtras).users
            val res = context.resources
            if (users.size == 1) {
                val displayName = manager.getDisplayName(users[0], nameFirst)
                return res.getString(R.string.message_format_participants_leave, displayName)
            } else {
                val usersName = res.getQuantityString(R.plurals.N_users, users.size, users.size)
                return res.getString(R.string.message_format_participants_leave, usersName)
            }
        }
        MessageType.CONVERSATION_NAME_UPDATE -> {
            extras as NameUpdatedExtras
            val res = context.resources
            if (extras.user != null) {
                return res.getString(R.string.message_format_conversation_name_update_by_user,
                        manager.getDisplayName(extras.user, nameFirst), extras.name)
            } else {
                return res.getString(R.string.message_format_conversation_name_update, extras.name)
            }
        }
    }
    return text
}