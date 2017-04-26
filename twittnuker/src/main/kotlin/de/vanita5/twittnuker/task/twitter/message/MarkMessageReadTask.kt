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

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.OrderBy
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.extension.model.timestamp
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableMessage
import de.vanita5.twittnuker.model.ParcelableMessageConversation
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.UnreadCountUpdatedEvent
import de.vanita5.twittnuker.model.message.conversation.TwitterOfficialConversationExtras
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages.Conversations
import de.vanita5.twittnuker.task.ExceptionHandlingAbstractTask
import de.vanita5.twittnuker.task.twitter.message.SendMessageTask.Companion.TEMP_CONVERSATION_ID_PREFIX
import de.vanita5.twittnuker.util.DataStoreUtils


class MarkMessageReadTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, Unit?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        if (conversationId.startsWith(TEMP_CONVERSATION_ID_PREFIX)) return true
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val conversation = DataStoreUtils.findMessageConversation(context, accountKey, conversationId)
        val lastReadEvent = conversation?.let {
            return@let performMarkRead(context, microBlog, account, conversation)
        } ?: return false
        updateLocalLastRead(context.contentResolver, accountKey, conversationId, lastReadEvent)
        return true
    }

    override fun onSucceed(callback: Unit?, result: Boolean) {
        bus.post(UnreadCountUpdatedEvent(-1))
    }


    companion object {

        @Throws(MicroBlogException::class)
        internal fun performMarkRead(context: Context, microBlog: MicroBlog, account: AccountDetails,
                conversation: ParcelableMessageConversation): Pair<String, Long>? {
            val cr = context.contentResolver
            when (account.type) {
                AccountType.TWITTER -> {
                    if (account.isOfficial(context)) {
                        val event = (conversation.conversation_extras as? TwitterOfficialConversationExtras)?.maxReadEvent ?: run {
                            val message = cr.findRecentMessage(account.key, conversation.id) ?: return null
                            return@run Pair(message.id, message.timestamp)
                        }
                        if (conversation.last_read_timestamp > event.second) {
                            // Local is newer, ignore network request
                            return event
                        }
                        if (microBlog.markDmRead(conversation.id, event.first).isSuccessful) {
                            return event
                        }
                    }
                }
            }
            val message = cr.findRecentMessage(account.key, conversation.id) ?: return null
            return Pair(message.id, message.timestamp)
        }

        internal fun updateLocalLastRead(cr: ContentResolver, accountKey: UserKey,
                conversationId: String, lastRead: Pair<String, Long>) {
            val values = ContentValues()
            values.put(Conversations.LAST_READ_ID, lastRead.first)
            values.put(Conversations.LAST_READ_TIMESTAMP, lastRead.second)
            val updateWhere = Expression.and(Expression.equalsArgs(Conversations.ACCOUNT_KEY),
                    Expression.equalsArgs(Conversations.CONVERSATION_ID),
                    Expression.lesserThan(Conversations.LAST_READ_TIMESTAMP,lastRead.second)).sql
            val updateWhereArgs = arrayOf(accountKey.toString(), conversationId)
            cr.update(Conversations.CONTENT_URI, values, updateWhere, updateWhereArgs)
        }

        private fun ContentResolver.findRecentMessage(accountKey: UserKey, conversationId: String): ParcelableMessage? {
            val where = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
            val whereArgs = arrayOf(accountKey.toString(), conversationId)
            @SuppressLint("Recycle")
            val cur = query(Messages.CONTENT_URI, Messages.COLUMNS,
                    where, whereArgs, OrderBy(Messages.LOCAL_TIMESTAMP, false).sql) ?: return null
            try {
                if (cur.moveToFirst()) {
                    val indices = ObjectCursor.indicesFrom(cur, ParcelableMessage::class.java)
                    return indices.newObject(cur)
                }
            } finally {
                cur.close()
            }
            return null
        }

        private val TwitterOfficialConversationExtras.maxReadEvent: Pair<String, Long>?
            get() {
                val id = maxEntryId ?: return null
                if (maxEntryTimestamp < 0) return null
                return Pair(id, maxEntryTimestamp)
            }

    }
}