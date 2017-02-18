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
import de.vanita5.twittnuker.library.MicroBlog
import de.vanita5.twittnuker.library.MicroBlogException
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages
import de.vanita5.twittnuker.task.ExceptionHandlingAbstractTask


class DestroyMessageTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String?,
        val messageId: String
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, Unit?>(context) {
    override fun onExecute(params: Unit?): Boolean {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        if (!performDestroyMessage(microBlog, account)) {
            return false
        }
        val deleteWhere: String
        val deleteWhereArgs: Array<String>
        if (conversationId != null) {
            deleteWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.CONVERSATION_ID),
                    Expression.equalsArgs(Messages.MESSAGE_ID)).sql
            deleteWhereArgs = arrayOf(accountKey.toString(), conversationId, messageId)
        } else {
            deleteWhere = Expression.and(Expression.equalsArgs(Messages.ACCOUNT_KEY),
                    Expression.equalsArgs(Messages.MESSAGE_ID)).sql
            deleteWhereArgs = arrayOf(accountKey.toString(), messageId)
        }
        context.contentResolver.delete(Messages.CONTENT_URI, deleteWhere, deleteWhereArgs)
        return true
    }

    private fun performDestroyMessage(microBlog: MicroBlog, account: AccountDetails): Boolean {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    return microBlog.destroyDm(messageId).isSuccessful
                }
            }
        }
        microBlog.destroyDirectMessage(messageId)
        return true
    }


}