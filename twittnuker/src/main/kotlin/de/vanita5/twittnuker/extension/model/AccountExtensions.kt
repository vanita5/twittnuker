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

package de.vanita5.twittnuker.extension.model

import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.toHexColor
import org.mariotaku.ktextension.toInt
import de.vanita5.twittnuker.TwittnukerConstants.*


fun android.accounts.Account.getCredentials(am: android.accounts.AccountManager): de.vanita5.twittnuker.model.account.cred.Credentials {
    val authToken = am.peekAuthToken(this, ACCOUNT_AUTH_TOKEN_TYPE) ?: run {
        throw IllegalStateException("AuthToken is null for ${this}")
    }
    return de.vanita5.twittnuker.extension.model.parseCredentials(authToken, getCredentialsType(am))
}

@de.vanita5.twittnuker.model.account.cred.Credentials.Type
fun android.accounts.Account.getCredentialsType(am: android.accounts.AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_CREDS_TYPE) ?: de.vanita5.twittnuker.model.account.cred.Credentials.Type.OAUTH
}

fun android.accounts.Account.getAccountKey(am: android.accounts.AccountManager): de.vanita5.twittnuker.model.UserKey {
    val accountKeyString = am.getUserData(this, ACCOUNT_USER_DATA_KEY) ?: run {
        throw IllegalStateException("UserKey is null for ${this}")
    }
    return de.vanita5.twittnuker.model.UserKey.valueOf(accountKeyString)
}

fun android.accounts.Account.setAccountKey(am: android.accounts.AccountManager, accountKey: de.vanita5.twittnuker.model.UserKey) {
    am.setUserData(this, ACCOUNT_USER_DATA_KEY, accountKey.toString())
}

fun android.accounts.Account.getAccountUser(am: android.accounts.AccountManager): de.vanita5.twittnuker.model.ParcelableUser {
    val user = com.bluelinelabs.logansquare.LoganSquare.parse(am.getUserData(this, ACCOUNT_USER_DATA_USER), de.vanita5.twittnuker.model.ParcelableUser::class.java)
    user.is_cache = true
    return user
}

fun android.accounts.Account.setAccountUser(am: android.accounts.AccountManager, user: de.vanita5.twittnuker.model.ParcelableUser) {
    am.setUserData(this, ACCOUNT_USER_DATA_USER, com.bluelinelabs.logansquare.LoganSquare.serialize(user))
}

@android.support.annotation.ColorInt
fun android.accounts.Account.getColor(am: android.accounts.AccountManager): Int {
    return de.vanita5.twittnuker.util.ParseUtils.parseColor(am.getUserData(this, ACCOUNT_USER_DATA_COLOR), 0)
}

fun android.accounts.Account.getPosition(am: android.accounts.AccountManager): Int {
    return am.getUserData(this, ACCOUNT_USER_DATA_POSITION).toInt(-1)
}

fun android.accounts.Account.getAccountExtras(am: android.accounts.AccountManager): de.vanita5.twittnuker.model.account.AccountExtras? {
    val json = am.getUserData(this, ACCOUNT_USER_DATA_EXTRAS) ?: return null
    when (getAccountType(am)) {
        de.vanita5.twittnuker.annotation.AccountType.TWITTER -> {
            return com.bluelinelabs.logansquare.LoganSquare.parse(json, de.vanita5.twittnuker.model.account.TwitterAccountExtras::class.java)
        }
        de.vanita5.twittnuker.annotation.AccountType.STATUSNET -> {
            return com.bluelinelabs.logansquare.LoganSquare.parse(json, de.vanita5.twittnuker.model.account.StatusNetAccountExtras::class.java)
        }
    }
    return null
}

@de.vanita5.twittnuker.annotation.AccountType
fun android.accounts.Account.getAccountType(am: android.accounts.AccountManager): String {
    return am.getUserData(this, ACCOUNT_USER_DATA_TYPE) ?: de.vanita5.twittnuker.annotation.AccountType.TWITTER
}

fun android.accounts.Account.isActivated(am: android.accounts.AccountManager): Boolean {
    return am.getUserData(this, ACCOUNT_USER_DATA_ACTIVATED).orEmpty().toBoolean()
}

fun android.accounts.Account.setActivated(am: android.accounts.AccountManager, activated: Boolean) {
    am.setUserData(this, ACCOUNT_USER_DATA_ACTIVATED, activated.toString())
}

fun android.accounts.Account.setColor(am: android.accounts.AccountManager, color: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_COLOR, toHexColor(color, format = HexColorFormat.RGB))
}

fun android.accounts.Account.setPosition(am: android.accounts.AccountManager, position: Int) {
    am.setUserData(this, ACCOUNT_USER_DATA_POSITION, position.toString())
}


private fun parseCredentials(authToken: String, @de.vanita5.twittnuker.model.account.cred.Credentials.Type authType: String): de.vanita5.twittnuker.model.account.cred.Credentials {
    when (authType) {
        de.vanita5.twittnuker.model.account.cred.Credentials.Type.OAUTH, de.vanita5.twittnuker.model.account.cred.Credentials.Type.XAUTH -> return com.bluelinelabs.logansquare.LoganSquare.parse(authToken, de.vanita5.twittnuker.model.account.cred.OAuthCredentials::class.java)
        de.vanita5.twittnuker.model.account.cred.Credentials.Type.BASIC -> return com.bluelinelabs.logansquare.LoganSquare.parse(authToken, de.vanita5.twittnuker.model.account.cred.BasicCredentials::class.java)
        de.vanita5.twittnuker.model.account.cred.Credentials.Type.EMPTY -> return com.bluelinelabs.logansquare.LoganSquare.parse(authToken, de.vanita5.twittnuker.model.account.cred.EmptyCredentials::class.java)
    }
    throw UnsupportedOperationException()
}