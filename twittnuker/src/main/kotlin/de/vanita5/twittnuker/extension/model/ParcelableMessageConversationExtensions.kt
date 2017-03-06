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
import de.vanita5.twittnuker.model.ParcelableMessageConversation
import de.vanita5.twittnuker.model.ParcelableMessageConversation.ConversationType
import de.vanita5.twittnuker.model.ParcelableMessageConversation.ExtrasType
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.message.conversation.DefaultConversationExtras
import de.vanita5.twittnuker.model.message.conversation.TwitterOfficialConversationExtras
import de.vanita5.twittnuker.util.UserColorNameManager
import java.util.*

fun ParcelableMessageConversation.applyFrom(message: ParcelableMessage, details: AccountDetails) {
    account_key = details.key
    account_color = details.color
    message_type = message.message_type
    message_timestamp = message.message_timestamp
    local_timestamp = message.local_timestamp
    sort_id = message.sort_id
    text_unescaped = message.text_unescaped
    media = message.media
    spans = message.spans
    message_extras = message.extras
    sender_key = message.sender_key
    recipient_key = message.recipient_key
    is_outgoing = message.is_outgoing
    request_cursor = message.request_cursor
}

val ParcelableMessageConversation.timestamp: Long
    get() = if (message_timestamp > 0) message_timestamp else local_timestamp


val ParcelableMessageConversation.user: ParcelableUser?
    get() {
        val userKey = if (is_outgoing) recipient_key else sender_key
        return participants.firstOrNull { it.key == userKey }
    }

val ParcelableMessageConversation.readOnly: Boolean
    get() {
        when (conversation_extras_type) {
            ExtrasType.TWITTER_OFFICIAL -> {
                return (conversation_extras as? TwitterOfficialConversationExtras)?.readOnly ?: false
            }
        }
        return false
    }

var ParcelableMessageConversation.notificationDisabled: Boolean
    get() {
        when (conversation_extras_type) {
            ExtrasType.TWITTER_OFFICIAL -> {
                return (conversation_extras as? TwitterOfficialConversationExtras)?.notificationsDisabled ?: false
            }
            else -> {
                return (conversation_extras as? DefaultConversationExtras)?.notificationsDisabled ?: false
            }
        }
    }
    set(value) {
        when (conversation_extras_type) {
            ExtrasType.TWITTER_OFFICIAL -> {
                val extras = conversation_extras as? TwitterOfficialConversationExtras ?: run {
                    val obj = TwitterOfficialConversationExtras()
                    conversation_extras = obj
                    return@run obj
                }
                extras.notificationsDisabled = value
            }
            else -> {
                val extras = conversation_extras as? DefaultConversationExtras ?: run {
                    val obj = DefaultConversationExtras()
                    conversation_extras = obj
                    return@run obj
                }
                extras.notificationsDisabled = value
            }
        }
    }

fun ParcelableMessageConversation.getTitle(context: Context, manager: UserColorNameManager,
        nameFirst: Boolean): Pair<String, String?> {
    if (conversation_type == ConversationType.ONE_TO_ONE) {
        val user = this.user ?: return Pair(context.getString(R.string.title_direct_messages), null)
        return Pair(user.name, "@${user.screen_name}")
    }
    if (conversation_name != null) {
        return Pair(conversation_name, null)
    }
    return Pair(participants.joinToString(separator = ", ") { manager.getDisplayName(it, nameFirst) }, null)
}

fun ParcelableMessageConversation.getSubtitle(context: Context): String? {
    if (conversation_type == ConversationType.ONE_TO_ONE) {
        val user = this.user ?: return null
        return "@${user.screen_name}"
    }
    val resources = context.resources
    return resources.getQuantityString(R.plurals.N_message_participants, participants.size,
            participants.size)
}

fun ParcelableMessageConversation.getSummaryText(context: Context, manager: UserColorNameManager,
        nameFirst: Boolean): CharSequence? {
    return getSummaryText(context, manager, nameFirst, message_type, message_extras, sender_key,
            text_unescaped, this)
}


fun ParcelableMessageConversation.addParticipants(users: Collection<ParcelableUser>) {
    val participants = this.participants
    if (participants == null) {
        this.participants = arrayOf(user)
    } else {
        val addingUsers = ArrayList<ParcelableUser>()
        users.forEach { user ->
            val index = participants.indexOfFirst { it.key == user.key }
            if (index >= 0) {
                participants[index] = user
            } else {
                addingUsers += user
            }
        }
        this.participants += addingUsers
    }
    this.participant_keys = this.participants.map(ParcelableUser::key).toTypedArray()
    this.participants.sortBy(ParcelableUser::screen_name)
}