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
import android.annotation.SuppressLint
import android.content.Context
import org.mariotaku.ktextension.useCursor
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.library.twitter.model.Paging
import de.vanita5.twittnuker.library.twitter.model.User
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.extension.model.setFrom
import de.vanita5.twittnuker.extension.model.timestamp
import de.vanita5.twittnuker.model.*
import de.vanita5.twittnuker.model.ParcelableMessageConversation.ConversationType
import de.vanita5.twittnuker.model.util.AccountUtils.getAccountDetails
import de.vanita5.twittnuker.model.util.ParcelableMessageUtils
import de.vanita5.twittnuker.model.util.ParcelableUserUtils
import de.vanita5.twittnuker.model.util.UserKeyUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages.Conversations
import de.vanita5.twittnuker.util.content.ContentResolverUtils


class GetMessagesTask(context: Context) : BaseAbstractTask<RefreshTaskParam, Unit, (Boolean) -> Unit>(context) {
    override fun doLongOperation(param: RefreshTaskParam) {
        val accountKeys = param.accountKeys
        val am = AccountManager.get(context)
        accountKeys.forEachIndexed { i, accountKey ->
            val details = getAccountDetails(am, accountKey, true) ?: return@forEachIndexed
            val microBlog = details.newMicroBlogInstance(context, true, cls = MicroBlog::class.java)
            val messages = try {
                getMessages(microBlog, details, param, i)
            } catch (e: MicroBlogException) {
                return@forEachIndexed
            }
            storeMessages(messages, details)
        }
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Unit) {
        callback?.invoke(true)
    }

    private fun getMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshTaskParam, index: Int): GetMessagesData {
        when (details.type) {
            AccountType.FANFOU -> {
                // Use fanfou DM api
                return getFanfouMessages(microBlog)
            }
            AccountType.TWITTER -> {
                // Use official DM api
                if (details.isOfficial(context)) {
                    return getTwitterOfficialMessages(microBlog, details, param, index)
                }
            }
        }
        // Use default method
        return getDefaultMessages(microBlog, details, param, index)
    }

    private fun getFanfouMessages(microBlog: MicroBlog): GetMessagesData {
        return GetMessagesData(emptyList(), emptyList())
    }

    private fun getTwitterOfficialMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshTaskParam, index: Int): GetMessagesData {
        return getDefaultMessages(microBlog, details, param, index)
    }

    private fun getDefaultMessages(microBlog: MicroBlog, details: AccountDetails, param: RefreshTaskParam, index: Int): GetMessagesData {
        val accountKey = details.key

        val sinceIds = if (param.hasSinceIds) param.sinceIds else null
        val maxIds = if (param.hasMaxIds) param.maxIds else null

        val received = microBlog.getDirectMessages(Paging().apply {
            count(100)
            val maxId = maxIds?.get(index)
            val sinceId = sinceIds?.get(index)
            if (maxId != null) {
                maxId(maxId)
            }
            if (sinceIds != null) {
                sinceId(sinceId)
            }
        })
        val sent = microBlog.getSentDirectMessages(Paging().apply {
            count(100)
            val accountsCount = param.accountKeys.size
            val maxId = maxIds?.get(accountsCount + index)
            val sinceId = sinceIds?.get(accountsCount + index)
            if (maxId != null) {
                maxId(maxId)
            }
            if (sinceId != null) {
                sinceId(sinceId)
            }
        })


        val insertMessages = arrayListOf<ParcelableMessage>()
        val conversations = hashMapOf<String, ParcelableMessageConversation>()

        val conversationIds = hashSetOf<String>()
        received.forEach {
            conversationIds.add(ParcelableMessageUtils.incomingConversationId(it.senderId, it.recipientId))
        }
        received.forEach {
            conversationIds.add(ParcelableMessageUtils.incomingConversationId(it.senderId, it.recipientId))
        }

        conversations.addLocalConversations(accountKey, conversationIds)

        received.forEach { dm ->
            val message = ParcelableMessageUtils.incomingMessage(accountKey, dm)
            insertMessages.add(message)
            conversations.addConversation(details, message, dm.sender, dm.recipient)
        }
        sent.forEach { dm ->
            val message = ParcelableMessageUtils.outgoingMessage(accountKey, dm)
            insertMessages.add(message)
            conversations.addConversation(details, message, dm.sender, dm.recipient)
        }
        return GetMessagesData(conversations.values, insertMessages)
    }

    @SuppressLint("Recycle")
    private fun MutableMap<String, ParcelableMessageConversation>.addLocalConversations(accountKey: UserKey, conversationIds: Set<String>) {
        val where = Expression.and(Expression.inArgs(Conversations.CONVERSATION_ID, conversationIds.size),
                Expression.equalsArgs(Conversations.ACCOUNT_KEY)).sql
        val whereArgs = conversationIds.toTypedArray() + accountKey.toString()
        return context.contentResolver.query(Conversations.CONTENT_URI, Conversations.COLUMNS,
                where, whereArgs, null).useCursor { cur ->
            val indices = ParcelableMessageConversationCursorIndices(cur)
            cur.moveToFirst()
            while (!cur.isAfterLast) {
                val conversationId = cur.getString(indices.id)
                val timestamp = cur.getLong(indices.local_timestamp)
                val conversation = this[conversationId] ?: run {
                    val obj = indices.newObject(cur)
                    this[conversationId] = obj
                    return@run obj
                }
                if (timestamp > conversation.local_timestamp) {
                    this[conversationId] = indices.newObject(cur)
                }
                indices.newObject(cur)
                cur.moveToNext()
            }
        }
    }

    private fun storeMessages(data: GetMessagesData, details: AccountDetails) {
        val resolver = context.contentResolver
        val conversationsValues = data.conversations.map {
            val values = ParcelableMessageConversationValuesCreator.create(it)
            if (it._id > 0) {
                values.put(Conversations._ID, it._id)
            }
            return@map values
        }
        val messagesValues = data.messages.map(ParcelableMessageValuesCreator::create)

        ContentResolverUtils.bulkInsert(resolver, Conversations.CONTENT_URI, conversationsValues)
        ContentResolverUtils.bulkInsert(resolver, Messages.CONTENT_URI, messagesValues)
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
            details: AccountDetails,
            message: ParcelableMessage,
            vararg users: User
    ) {
        val conversation = this[message.conversation_id] ?: run {
            val obj = ParcelableMessageConversation()
            obj.id = message.conversation_id
            obj.conversation_type = ConversationType.ONE_TO_ONE
            obj.setFrom(message, details)
            this[message.conversation_id] = obj
            return@run obj
        }
        if (message.timestamp > conversation.timestamp) {
            conversation.setFrom(message, details)
        }
        users.forEach { user ->
            conversation.addParticipant(details.key, user)
        }
    }


    data class GetMessagesData(
            val conversations: Collection<ParcelableMessageConversation>,
            val messages: Collection<ParcelableMessage>
    )
}
