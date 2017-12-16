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

package de.vanita5.twittnuker.task.twitter.message

import android.accounts.AccountManager
import android.content.Context
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.extension.queryReference
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages
import de.vanita5.twittnuker.task.ExceptionHandlingAbstractTask
import de.vanita5.twittnuker.util.content.ContentResolverUtils


class ClearMessagesTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, ((Boolean) -> Unit)?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val messagesWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                Expression.equalsArgs(Messages.CONVERSATION_ID)).sql
        val messagesWhereArgs = arrayOf(accountKey.toString(), conversationId)
        val projection = arrayOf(Messages.MESSAGE_ID)
        val messageIds = mutableListOf<String>()
        context.contentResolver.queryReference(Messages.CONTENT_URI, projection, messagesWhere,
                messagesWhereArgs, null)?.use { (cur) ->
            cur.moveToFirst()
            while (!cur.isAfterLast) {
                val messageId = cur.getString(0)
                try {
                    if (DestroyMessageTask.performDestroyMessage(context, microBlog, account,
                            messageId)) {
                        messageIds.add(messageId)
                    }
                } catch (e: MicroBlogException) {
                    // Ignore
                }
                cur.moveToNext()
            }
        }
        ContentResolverUtils.bulkDelete(context.contentResolver, Messages.CONTENT_URI,
                Messages.MESSAGE_ID, false, messageIds, messagesWhere, messagesWhereArgs)
        return true
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Boolean?, exception: MicroBlogException?) {
        callback?.invoke(result ?: false)
    }
}