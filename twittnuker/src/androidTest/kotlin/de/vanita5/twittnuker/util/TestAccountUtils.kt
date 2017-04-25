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

package de.vanita5.twittnuker.util

import android.accounts.AccountManager
import android.support.test.InstrumentationRegistry
import de.vanita5.twittnuker.extension.model.updateDetails
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.test.R
import de.vanita5.twittnuker.util.support.removeAccountSupport

object TestAccountUtils {

    private val accountResources = intArrayOf(R.raw.account_4223092274_twitter_com)

    fun insertTestAccounts() {
        val targetContext = InstrumentationRegistry.getTargetContext()
        val context = InstrumentationRegistry.getContext()
        val am = AccountManager.get(targetContext)
        val existingAccounts = AccountUtils.getAllAccountDetails(am, false)
        accountResources.forEach { resId ->
            val details = context.resources.openRawResource(resId).use {
                JsonSerializer.parse(it, AccountDetails::class.java)
            }
            if (existingAccounts.any { it.account == details.account || it.key == details.key }) {
                return@forEach
            }
            am.addAccountExplicitly(details.account, null, null)
            details.account.updateDetails(am, details)
        }
    }

    fun removeTestAccounts() {
        val targetContext = InstrumentationRegistry.getTargetContext()
        val am = AccountManager.get(targetContext)
        val existingAccounts = AccountUtils.getAllAccountDetails(am, false)
        existingAccounts.filter { it.test }.forEach { am.removeAccountSupport(it.account) }
    }
}