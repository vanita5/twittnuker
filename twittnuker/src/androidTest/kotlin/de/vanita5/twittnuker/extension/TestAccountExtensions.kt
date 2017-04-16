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

package de.vanita5.twittnuker.extension

import android.accounts.Account
import android.accounts.AccountManager
import de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_TEST
import de.vanita5.twittnuker.extension.model.AccountDataQueue


fun Account.isTest(am: AccountManager): Boolean {
    return AccountDataQueue.getUserData(am, this, ACCOUNT_USER_DATA_TEST)?.toBoolean() ?: true
}

fun Account.setTest(am: AccountManager, test: Boolean) {
    am.setUserData(this, ACCOUNT_USER_DATA_TEST, test.toString())
}