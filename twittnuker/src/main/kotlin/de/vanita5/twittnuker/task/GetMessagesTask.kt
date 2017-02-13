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

package de.vanita5.twittnuker.task

import android.accounts.AccountManager
import android.content.Context
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.User
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.extension.model.setFrom
import de.vanita5.twittnuker.extension.model.timestamp
import de.vanita5.twittnuker.model.*
import de.vanita5.twittnuker.model.util.AccountUtils.getAccountDetails
import de.vanita5.twittnuker.model.util.ParcelableMessageUtils
import de.vanita5.twittnuker.model.util.ParcelableUserUtils
import de.vanita5.twittnuker.model.util.UserKeyUtils
import de.vanita5.twittnuker.util.DebugLog


class GetMessagesTask(context: Context) : BaseAbstractTask<RefreshTaskParam, Unit, (Boolean) -> Unit>(context) {
    override fun doLongOperation(param: RefreshTaskParam) {
        val accountKeys = param.accountKeys
        val am = AccountManager.get(context)
        accountKeys.forEachIndexed { i, accountKey ->
            val details = getAccountDetails(am, accountKey, true) ?: return@forEachIndexed
            val microBlog = details.newMicroBlogInstance(context, true, cls = MicroBlog::class.java)
            val messages = try {
                getMessages(microBlog, details)
            } catch (e: MicroBlogException) {
                return@forEachIndexed
            }
            storeMessages(messages)
        }
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Unit) {
        callback?.invoke(true)
    }

    private fun getMessages(microBlog: MicroBlog, details: AccountDetails): GetMessagesData {
        when (details.type) {
            AccountType.FANFOU -> {
                // Use fanfou DM api
                return getFanfouMessages(microBlog)
            }
            AccountType.TWITTER -> {
                // Use official DM api
                if (details.isOfficial(context)) {
                    return getTwitterOfficialMessages(microBlog, details)
                }
            }
        }
        // Use default method
        return getDefaultMessages(microBlog, details)
    }

    private fun getFanfouMessages(microBlog: MicroBlog): GetMessagesData {
        return GetMessagesData(emptyList(), emptyList(), emptyList())
    }

    private fun getTwitterOfficialMessages(microBlog: MicroBlog, details: AccountDetails): GetMessagesData {
        return getDefaultMessages(microBlog, details)
    }

    private fun getDefaultMessages(microBlog: MicroBlog, details: AccountDetails): GetMessagesData {
        val accountKey = details.key
        val paging = Paging()
        val insertMessages = arrayListOf<ParcelableMessage>()
        val conversations = hashMapOf<String, ParcelableMessageConversation>()
        microBlog.getDirectMessages(paging).forEach { dm ->
            val message = ParcelableMessageUtils.incomingMessage(accountKey, dm)
            insertMessages.add(message)
            conversations.addConversation(accountKey, message, dm.sender, dm.recipient)
        }
        microBlog.getSentDirectMessages(paging).forEach { dm ->
            val message = ParcelableMessageUtils.outgoingMessage(accountKey, dm)
            insertMessages.add(message)
            conversations.addConversation(accountKey, message, dm.sender, dm.recipient)
        }
        return GetMessagesData(conversations.values, emptyList(), insertMessages)
    }

    private fun storeMessages(data: GetMessagesData) {
        DebugLog.d(LOGTAG, data.toString())
    }

    private fun ParcelableMessageConversation.addParticipant(
            accountKey: UserKey,
            user: User
    ) {
            val userKey = UserKeyUtils.fromUser(user)
            val participants = this.participants
            if (participants == null) {
                this.participants = arrayOf(ParcelableUserUtils.fromUser(user, accountKey))
            } else {
                val index = participants.indexOfFirst { it.key == userKey }
                if (index >= 0) {
                    participants[index] = ParcelableUserUtils.fromUser(user, accountKey)
                } else {
                    this.participants = participants + ParcelableUserUtils.fromUser(user, accountKey)
                }
            }
        }

    private fun MutableMap<String, ParcelableMessageConversation>.addConversation(
            accountKey: UserKey,
            message: ParcelableMessage,
            vararg users: User
    ) {
        val conversation = this[message.conversation_id] ?: run {
            val obj = ParcelableMessageConversation()
            this[message.conversation_id] = obj
        obj.setFrom(message)
            return@run obj
        }
        if (message.timestamp > conversation.timestamp) {
            conversation.setFrom(message)
        }
        users.forEach { user ->
            conversation.addParticipant(accountKey, user)
        }
    }


    data class GetMessagesData(
            val insertConversations: Collection<ParcelableMessageConversation>,
            val updateConversations: Collection<ParcelableMessageConversation>,
            val insertMessages: Collection<ParcelableMessage>
    )
}
