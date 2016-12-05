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

package de.vanita5.twittnuker.account

import android.accounts.Account
import android.accounts.AccountManager
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.ktextension.Bundle
import de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_TYPE
import de.vanita5.twittnuker.extension.model.account_name
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts
import de.vanita5.twittnuker.util.support.AccountManagerSupport

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @Test
    fun testMigration() {

        val context = InstrumentationRegistry.getTargetContext()

        val am = AccountManager.get(context)

        am.getAccountsByType(ACCOUNT_TYPE).map { account ->
            AccountManagerSupport.removeAccount(am, account, null, null, null)
        }

        ParcelableAccountUtils.getAccounts(context).forEach { pAccount ->
            val account = Account(pAccount.account_name, ACCOUNT_TYPE)
            val userdata = Bundle {
                this[Accounts.ACCOUNT_KEY]
            }
            am.addAccountExplicitly(account, null, userdata)
        }
    }
}