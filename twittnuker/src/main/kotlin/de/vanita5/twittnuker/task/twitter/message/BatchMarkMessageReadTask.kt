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
import android.content.Context
import org.mariotaku.ktextension.forEachRow
import org.mariotaku.ktextension.useCursor
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Columns
import org.mariotaku.sqliteqb.library.Expression
import org.mariotaku.sqliteqb.library.Table
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.ParcelableMessageConversation
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.event.UnreadCountUpdatedEvent
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages.Conversations
import de.vanita5.twittnuker.task.ExceptionHandlingAbstractTask
import de.vanita5.twittnuker.util.TwidereQueryBuilder
import de.vanita5.twittnuker.util.getUnreadMessagesEntriesCursor


class BatchMarkMessageReadTask(
        context: Context,
        val accountKey: UserKey,
        val markTimestampBefore: Long
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, Unit?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        val cr = context.contentResolver
        val projection = (Conversations.COLUMNS + Conversations.UNREAD_COUNT).map {
            TwidereQueryBuilder.mapConversationsProjection(it)
        }.toTypedArray()

        val unreadWhere = Expression.greaterThan(Columns.Column(Table(Conversations.TABLE_NAME),
                Conversations.LAST_READ_TIMESTAMP), markTimestampBefore)
        val unreadHaving = Expression.greaterThan(Conversations.UNREAD_COUNT, 0)
        val cur = cr.getUnreadMessagesEntriesCursor(projection, arrayOf(accountKey),
                unreadWhere, null, unreadHaving, null) ?: return false

        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        cur.useCursor {
            val indices = ObjectCursor.indicesFrom(cur, ParcelableMessageConversation::class.java)
            cur.forEachRow { cur, _ ->
                val conversation = indices.newObject(cur)
                try {
                    val lastReadEvent = MarkMessageReadTask.performMarkRead(context, microBlog,
                            account, conversation) ?: return@forEachRow false
                    MarkMessageReadTask.updateLocalLastRead(cr, account.key, conversation.id,
                            lastReadEvent)
                    return@forEachRow true
                } catch (e: MicroBlogException) {
                    return@forEachRow false
                }
            }
        }
        return true
    }

    override fun onSucceed(callback: Unit?, result: Boolean) {
        bus.post(UnreadCountUpdatedEvent(-1))
    }
}