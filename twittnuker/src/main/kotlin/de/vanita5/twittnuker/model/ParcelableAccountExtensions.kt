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

package de.vanita5.twittnuker.model

import android.accounts.Account
import android.accounts.AccountManager
import de.vanita5.twittnuker.extension.*


fun Account.toParcelableAccount(am: AccountManager): ParcelableAccount {
    val account = ParcelableAccount()
    writeParcelableAccount(am, account)
    return account
}

internal fun Account.writeParcelableAccount(am: AccountManager, account: ParcelableAccount) {
    val user = getAccountUser(am)
    val activated = isAccountActivated(am)
    val accountKey = getAccountKey(am)
    val accountType = getAccountType(am)

    account.account_key = accountKey
    account.account_type = accountType
    account.is_activated = activated

    account.screen_name = user.screen_name
    account.name = user.name
    account.profile_banner_url = user.profile_banner_url
    account.profile_image_url = user.profile_image_url

    account.account_user = user
    account.color = getColor(am)
}
