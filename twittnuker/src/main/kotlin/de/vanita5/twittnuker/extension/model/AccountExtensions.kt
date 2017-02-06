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

package de.vanita5.twittnuker.extension.model

import android.accounts.Account
import android.accounts.AccountManager
import com.bluelinelabs.logansquare.LoganSquare
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.toHexColor
import org.mariotaku.ktextension.toInt
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.account.AccountExtras
import de.vanita5.twittnuker.model.account.StatusNetAccountExtras
import de.vanita5.twittnuker.model.account.TwitterAccountExtras
import de.vanita5.twittnuker.model.account.cred.BasicCredentials
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.account.cred.EmptyCredentials
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.util.ParseUtils


fun Account.getCredentials(am: AccountManager): Credentials {
    val authToken = am.peekAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE) ?: run {
        for (i in 0 until 5) {
            Thread.sleep(50L)
            val token = am.peekAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE)
            if (token != null) return@run token
        }
        throw NullPointerException("AuthToken is null for ${this}")
    }
    return parseCredentials(authToken, getCredentialsType(am))
}

@Credentials.Type
fun Account.getCredentialsType(am: AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_CREDS_TYPE) ?: Credentials.Type.OAUTH
}

fun Account.getAccountKey(am: AccountManager): UserKey {
    return UserKey.valueOf(am.getNonNullUserData(this, ACCOUNT_USER_DATA_KEY))
}

fun Account.setAccountKey(am: AccountManager, accountKey: UserKey) {
    am.setUserData(this, ACCOUNT_USER_DATA_KEY, accountKey.toString())
}

fun Account.getAccountUser(am: AccountManager): ParcelableUser {
    val user = LoganSquare.parse(am.getNonNullUserData(this, ACCOUNT_USER_DATA_USER), ParcelableUser::class.java)
    user.is_cache = true
    return user
}

fun Account.setAccountUser(am: AccountManager, user: ParcelableUser) {
    am.setUserData(this, ACCOUNT_USER_DATA_USER, LoganSquare.serialize(user))
}

@android.support.annotation.ColorInt
fun Account.getColor(am: AccountManager): Int {
    return ParseUtils.parseColor(am.getUserData(this, ACCOUNT_USER_DATA_COLOR), 0)
}

fun Account.getPosition(am: AccountManager): Int {
    return am.getUserData(this, ACCOUNT_USER_DATA_POSITION).toInt(-1)
}

fun Account.getAccountExtras(am: AccountManager): AccountExtras? {
    val json = am.getUserData(this, ACCOUNT_USER_DATA_EXTRAS) ?: return null
    when (getAccountType(am)) {
        AccountType.TWITTER -> {
            return LoganSquare.parse(json, TwitterAccountExtras::class.java)
        }
        AccountType.STATUSNET -> {
            return LoganSquare.parse(json, StatusNetAccountExtras::class.java)
        }
    }
    return null
}

@AccountType
fun Account.getAccountType(am: AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_TYPE) ?: AccountType.TWITTER
}

fun Account.isActivated(am: AccountManager): Boolean {
    return am.getUserData(this, ACCOUNT_USER_DATA_ACTIVATED)?.toBoolean() ?: true
}

fun Account.setActivated(am: AccountManager, activated: Boolean) {
    am.setUserData(this, ACCOUNT_USER_DATA_ACTIVATED, activated.toString())
}

fun Account.setColor(am: AccountManager, color: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_COLOR, toHexColor(color, format = HexColorFormat.RGB))
}

fun Account.setPosition(am: AccountManager, position: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_POSITION, position.toString())
}

private fun AccountManager.getNonNullUserData(account: Account, key: String): String {
    return getUserData(account, key) ?: run {
        // Rare case, assume account manager service is not running
        for (i in 0 until 5) {
            Thread.sleep(50L)
            val data = getUserData(account, key)
            if (data != null) return data
        }
        throw NullPointerException("NonNull userData $key is null for $account")
    }
}

private fun parseCredentials(authToken: String, @Credentials.Type authType: String): Credentials {
    when (authType) {
        Credentials.Type.OAUTH, Credentials.Type.XAUTH -> return LoganSquare.parse(authToken, OAuthCredentials::class.java)
        Credentials.Type.BASIC -> return LoganSquare.parse(authToken, BasicCredentials::class.java)
        Credentials.Type.EMPTY -> return LoganSquare.parse(authToken, EmptyCredentials::class.java)
    }
    throw UnsupportedOperationException()
}