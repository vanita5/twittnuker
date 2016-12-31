/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util.support

import android.accounts.*
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import java.io.IOException
import java.util.concurrent.TimeUnit


fun AccountManager.removeAccountSupport(
                      account: Account,
        activity: Activity? = null,
        callback: AccountManagerCallback<Bundle>? = null,
        handler: Handler? = null
): AccountManagerFuture<Bundle> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        return AccountManagerSupportL.removeAccount(this, account, activity, callback, handler)
    }

    val future = this.removeAccount(account, { future ->
        callback?.run(BooleanToBundleAccountManagerFuture(future))
    }, handler)
    return BooleanToBundleAccountManagerFuture(future)
}

object AccountManagerSupportL {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    internal fun removeAccount(
            am: AccountManager, account: Account,
            activity: Activity?,
            callback: AccountManagerCallback<Bundle>?,
            handler: Handler?
    ): AccountManagerFuture<Bundle> {
        return am.removeAccount(account, activity, callback, handler)
    }

}

private class BooleanToBundleAccountManagerFuture internal constructor(private val future: AccountManagerFuture<Boolean>) : AccountManagerFuture<Bundle> {

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return future.cancel(mayInterruptIfRunning)
    }

    override fun isCancelled(): Boolean {
        return future.isCancelled
    }

    override fun isDone(): Boolean {
        return future.isDone
    }

    @Throws(OperationCanceledException::class, IOException::class, AuthenticatorException::class)
    override fun getResult(): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, future.result)
        return result
    }

    @Throws(OperationCanceledException::class, IOException::class, AuthenticatorException::class)
    override fun getResult(timeout: Long, unit: TimeUnit): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, future.getResult(timeout, unit))
        return result
    }
}