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
import de.vanita5.microblog.library.MicroBlog
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.isOfficial
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.extension.model.notificationDisabled
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.task.ExceptionHandlingAbstractTask
import de.vanita5.twittnuker.util.DataStoreUtils


class SetConversationNotificationDisabledTask(
        context: Context,
        val accountKey: UserKey,
        val conversationId: String,
        val notificationDisabled: Boolean
) : ExceptionHandlingAbstractTask<Unit?, Boolean, MicroBlogException, ((Boolean) -> Unit)?>(context) {

    override val exceptionClass = MicroBlogException::class.java

    override fun onExecute(params: Unit?): Boolean {
        val account = AccountUtils.getAccountDetails(AccountManager.get(context), accountKey, true) ?:
                throw MicroBlogException("No account")
        val microBlog = account.newMicroBlogInstance(context, cls = MicroBlog::class.java)
        val addData = requestSetNotificationDisabled(microBlog, account)
        GetMessagesTask.storeMessages(context, addData, account)
        return true
    }

    override fun afterExecute(callback: ((Boolean) -> Unit)?, result: Boolean?, exception: MicroBlogException?) {
        callback?.invoke(result ?: false)
    }

    private fun requestSetNotificationDisabled(microBlog: MicroBlog, account: AccountDetails):
            GetMessagesTask.DatabaseUpdateData {
        when (account.type) {
            AccountType.TWITTER -> {
                if (account.isOfficial(context)) {
                    val response = if (notificationDisabled) {
                        microBlog.disableDmConversations(conversationId)
                    } else {
                        microBlog.enableDmConversations(conversationId)
                    }
                    val conversation = DataStoreUtils.findMessageConversation(context, accountKey,
                            conversationId) ?: return GetMessagesTask.DatabaseUpdateData(emptyList(), emptyList())
                    if (response.isSuccessful) {
                        conversation.notificationDisabled = notificationDisabled
                    }
                    return GetMessagesTask.DatabaseUpdateData(listOf(conversation), emptyList())
                }
            }
        }

        val conversation = DataStoreUtils.findMessageConversation(context, accountKey,
                conversationId) ?: return GetMessagesTask.DatabaseUpdateData(emptyList(), emptyList())
        conversation.notificationDisabled = notificationDisabled
        // Don't finish too fast
        Thread.sleep(300L)
        return GetMessagesTask.DatabaseUpdateData(listOf(conversation), emptyList())
    }

}