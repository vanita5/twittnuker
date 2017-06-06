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

package de.vanita5.twittnuker.task.twitter.message

import android.content.Context
import org.mariotaku.ktextension.isNotNullOrEmpty
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.microblog.library.twitter.TwitterUpload
import de.vanita5.microblog.library.twitter.annotation.MediaCategory
import de.vanita5.microblog.library.twitter.model.DirectMessage
import de.vanita5.microblog.library.twitter.model.NewDm
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.api.*
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.model.ParcelableMessageConversation
import de.vanita5.twittnuker.model.ParcelableNewMessage
import de.vanita5.twittnuker.model.event.SendMessageTaskEvent
import de.vanita5.twittnuker.model.util.ParcelableMessageUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages.Conversations
import de.vanita5.twittnuker.task.ExceptionHandlingAbstractTask
import de.vanita5.twittnuker.task.twitter.UpdateStatusTask
import de.vanita5.twittnuker.task.twitter.message.GetMessagesTask
import de.vanita5.twittnuker.task.twitter.message.GetMessagesTask.Companion.addConversation
import de.vanita5.twittnuker.task.twitter.message.GetMessagesTask.Companion.addLocalConversations

class SendMessageTask(
        context: Context
) : ExceptionHandlingAbstractTask<ParcelableNewMessage, SendMessageTask.SendMessageResult,
        MicroBlogException, Unit>(context) {

    private val profileImageSize = context.getString(R.string.profile_image_size)

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: ParcelableNewMessage): SendMessageResult {
        val account = params.account ?: throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val updateData = requestSendMessage(microBlog, account, params)
        if (params.is_temp_conversation && params.conversation_id != null) {
            val deleteTempWhere = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID)).sql
            val deleteTempWhereArgs = arrayOf(account.key.toString(), params.conversation_id)
            context.contentResolver.delete(Conversations.CONTENT_URI, deleteTempWhere,
                    deleteTempWhereArgs)
        }
        GetMessagesTask.storeMessages(context, updateData, account)
        return SendMessageResult(updateData.conversations.map { it.id })
    }

    override fun onException(callback: Unit?, exception: MicroBlogException) {
        bus.post(SendMessageTaskEvent(params.account.key, params.conversation_id, null, false))
    }

    override fun onSucceed(callback: Unit?, result: SendMessageResult) {
        bus.post(SendMessageTaskEvent(params.account.key, params.conversation_id,
                result.conversationIds.singleOrNull(), true))
    }

    private fun requestSendMessage(microBlog: MicroBlog, account: AccountDetails,
            message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    return sendTwitterOfficialDM(microBlog, account, message)
                } else {
                    return sendTwitterMessageEvent(microBlog, account, message)
                }
            }
            AccountType.FANFOU -> {
                return sendFanfouDM(microBlog, account, message)
            }
        }
        return sendDefaultDM(microBlog, account, message)
    }

    private fun sendTwitterOfficialDM(microBlog: MicroBlog, account: AccountDetails,
            message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        var deleteOnSuccess: List<UpdateStatusTask.MediaDeletionItem>? = null
        var deleteAlways: List<UpdateStatusTask.MediaDeletionItem>? = null
        val sendResponse = try {
            val conversationId = message.conversation_id
            val tempConversation = message.is_temp_conversation

            val newDm = NewDm()
            if (!tempConversation && conversationId != null) {
                newDm.setConversationId(conversationId)
            } else {
                newDm.setRecipientIds(message.recipient_ids)
            }
            newDm.setText(message.text)

            if (message.media.isNotNullOrEmpty()) {
                val upload = account.newMicroBlogInstance(context, cls = TwitterUpload::class.java)
                val uploadResult = UpdateStatusTask.uploadMicroBlogMediaShared(context,
                        upload, account, message.media, null, null, true, null)
                newDm.setMediaId(uploadResult.ids[0])
                deleteAlways = uploadResult.deleteAlways
                deleteOnSuccess = uploadResult.deleteOnSuccess
            }
            microBlog.sendDm(newDm)
        } catch (e: UpdateStatusTask.UploadException) {
            e.deleteAlways?.forEach {
                it.delete(context)
            }
            throw MicroBlogException(e)
        } finally {
            deleteAlways?.forEach { it.delete(context) }
        }
        deleteOnSuccess?.forEach { it.delete(context) }
        val conversationId = sendResponse.entries?.firstOrNull {
            it.message != null
        }?.message?.conversationId
        val response = microBlog.getDmConversation(conversationId, null).conversationTimeline
        return GetMessagesTask.createDatabaseUpdateData(context, account, response, profileImageSize)
    }

    private fun sendTwitterMessageEvent(microBlog: MicroBlog, account: AccountDetails,
            message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        val recipientId = message.recipient_ids.singleOrNull() ?: throw MicroBlogException("No recipient")
        val category = when (message.media?.firstOrNull()?.type) {
            ParcelableMedia.Type.IMAGE -> MediaCategory.DM_IMAGE
            ParcelableMedia.Type.VIDEO -> MediaCategory.DM_VIDEO
            ParcelableMedia.Type.ANIMATED_GIF -> MediaCategory.DM_GIF
            else -> null
        }
        val response = uploadMediaThen(account, message, category) { mediaId ->
            val obj = DirectMessageEventObject {
                type = "message_create"
                messageCreate {
                    target { this.recipientId = recipientId }
                    messageData {
                        text = message.text
                        if (mediaId != null) {
                            attachment {
                                type = "media"
                                media {
                                    id = mediaId
                                }
                            }
                        }
                    }
                }
            }
            return@uploadMediaThen microBlog.newDirectMessageEvent(obj)
        }
        return createDatabaseUpdateData(account, microBlog.showDirectMessage(response.event.id))
    }

    private fun sendFanfouDM(microBlog: MicroBlog, account: AccountDetails, message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        val recipientId = message.recipient_ids.singleOrNull() ?: throw MicroBlogException("No recipient")
        val response = microBlog.sendFanfouDirectMessage(recipientId, message.text)
        return createDatabaseUpdateData(account, response)
    }

    private fun sendDefaultDM(microBlog: MicroBlog, account: AccountDetails, message: ParcelableNewMessage): GetMessagesTask.DatabaseUpdateData {
        val recipientId = message.recipient_ids.singleOrNull() ?: throw MicroBlogException("No recipient")
        val response = uploadMediaThen(account, message) { mediaId ->
            if (mediaId != null) {
                microBlog.sendDirectMessage(recipientId, message.text, mediaId)
            } else {
                microBlog.sendDirectMessage(recipientId, message.text)
            }
        }
        return createDatabaseUpdateData(account, response)
    }

    private fun <T> uploadMediaThen(account: AccountDetails, message: ParcelableNewMessage,
            category: String? = null, action: (mediaId: String?) -> T): T {
        var deleteOnSuccess: List<UpdateStatusTask.MediaDeletionItem>? = null
        var deleteAlways: List<UpdateStatusTask.MediaDeletionItem>? = null
        try {
            var mediaId: String? = null
            if (message.media.isNotNullOrEmpty()) {
                val upload = account.newMicroBlogInstance(context, cls = TwitterUpload::class.java)
                val uploadResult = UpdateStatusTask.uploadMicroBlogMediaShared(context,
                        upload, account, message.media, category, null, true, null)
                mediaId = uploadResult.ids[0]
                deleteAlways = uploadResult.deleteAlways
                deleteOnSuccess = uploadResult.deleteOnSuccess
            }
            val result = action(mediaId)
            deleteOnSuccess?.forEach { it.delete(context) }
            return result
        } catch (e: UpdateStatusTask.UploadException) {
            e.deleteAlways?.forEach {
                it.delete(context)
            }
            throw MicroBlogException(e)
        } finally {
            deleteAlways?.forEach { it.delete(context) }
        }
    }

    private fun createDatabaseUpdateData(details: AccountDetails, dm: DirectMessage): GetMessagesTask.DatabaseUpdateData {
        val accountKey = details.key
        val conversationIds = setOf(ParcelableMessageUtils.outgoingConversationId(dm.senderId, dm.recipientId))
        val conversations = hashMapOf<String, ParcelableMessageConversation>()
        conversations.addLocalConversations(context, accountKey, conversationIds)
        val message = ParcelableMessageUtils.fromMessage(accountKey, dm, true)
        val sender = dm.sender.toParcelable(details, profileImageSize = profileImageSize)
        val recipient = dm.recipient.toParcelable(details, profileImageSize = profileImageSize)
        conversations.addConversation(message.conversation_id, details, message, setOf(sender, recipient), appendUsers = true)
        return GetMessagesTask.DatabaseUpdateData(conversations.values, listOf(message))
    }

    class SendMessageResult(var conversationIds: List<String>)

    companion object {
        const val TEMP_CONVERSATION_ID_PREFIX = "twittnuker:temp:"
    }
}