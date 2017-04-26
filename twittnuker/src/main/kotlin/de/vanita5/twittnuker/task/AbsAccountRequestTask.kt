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

package de.vanita5.twittnuker.task

import android.accounts.AccountManager
import android.content.Context
import android.widget.Toast
import de.vanita5.microblog.library.MicroBlogException
import de.vanita5.twittnuker.exception.AccountNotFoundException
import de.vanita5.twittnuker.extension.getErrorMessage
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils


abstract class AbsAccountRequestTask<Params, Result, Callback>(context: Context, val accountKey: UserKey?) :
        ExceptionHandlingAbstractTask<Params, Result, MicroBlogException, Callback>(context) {
    override final val exceptionClass = MicroBlogException::class.java

    override final fun onExecute(params: Params): Result {
        val am = AccountManager.get(context)
        val account = accountKey?.let { AccountUtils.getAccountDetails(am, it, true) } ?:
                throw AccountNotFoundException()
        try {
            val result = onExecute(account, params)
            onCleanup(account, params, result, null)
            return result
        } catch (e: MicroBlogException) {
            onCleanup(account, params, null, e)
            throw e
        }
    }

    protected abstract fun onExecute(account: AccountDetails, params: Params): Result

    protected open fun onCleanup(account: AccountDetails, params: Params, result: Result?, exception: MicroBlogException?) {
        if (result != null) {
            onCleanup(account, params, result)
        } else if (exception != null) {
            onCleanup(account, params, exception)
        }
    }

    protected open fun onCleanup(account: AccountDetails, params: Params, result: Result) {}
    protected open fun onCleanup(account: AccountDetails, params: Params, exception: MicroBlogException) {}

    override fun onException(callback: Callback?, exception: MicroBlogException) {
        Toast.makeText(context, exception.getErrorMessage(context), Toast.LENGTH_SHORT).show()
    }
}