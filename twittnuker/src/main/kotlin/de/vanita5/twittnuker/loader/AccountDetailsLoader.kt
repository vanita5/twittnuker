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

package de.vanita5.twittnuker.loader

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.content.Context
import android.support.v4.content.AsyncTaskLoader
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.util.AccountUtils

class AccountDetailsLoader(
        context: Context,
        val filter: (AccountDetails.() -> Boolean)? = null
) : AsyncTaskLoader<List<AccountDetails>>(context) {
    private val am: AccountManager
    private val accountUpdateListener = OnAccountsUpdateListener {
        onContentChanged()
    }

    init {
        am = AccountManager.get(context)
    }

    override fun loadInBackground(): List<AccountDetails> {
        return AccountUtils.getAllAccountDetails(am).filter {
            filter?.invoke(it) ?: true
        }.sortedBy(AccountDetails::position)
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
        am.removeOnAccountsUpdatedListenerSafe(accountUpdateListener)
    }

    override fun onStartLoading() {
        am.addOnAccountsUpdatedListenerSafe(accountUpdateListener, updateImmediately = true)
        if (takeContentChanged()) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        cancelLoad()
    }
}