/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util

import android.accounts.Account
import android.accounts.AccountManager
import android.database.sqlite.SQLiteDatabase
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.toHexColor
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.annotation.AuthTypeInt
import de.vanita5.twittnuker.model.ParcelableCredentials
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.account.cred.BasicCredentials
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.account.cred.EmptyCredentials
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts

/**
 * Migrate legacy credentials to system account framework
 */
@Suppress("deprecation")
fun migrateAccounts(am: AccountManager, db: SQLiteDatabase) {
    val cur = db.query(Accounts.TABLE_NAME, Accounts.COLUMNS, null, null, null, null, null) ?: return
    try {
        val indices = ObjectCursor.indicesFrom(cur, ParcelableCredentials::class.java)
        cur.moveToFirst()
        while (!cur.isAfterLast) {
            val credentials = indices.newObject(cur)
            val account = Account(credentials.account_name, ACCOUNT_TYPE)
            // Don't add UserData in this method, see http://stackoverflow.com/a/29776224/859190
            am.addAccountExplicitly(account, null, null)
            am.setUserData(account, ACCOUNT_USER_DATA_KEY, credentials.account_key.toString())
            am.setUserData(account, ACCOUNT_USER_DATA_TYPE, credentials.account_type)
            am.setUserData(account, ACCOUNT_USER_DATA_ACTIVATED, credentials.is_activated.toString())
            am.setUserData(account, ACCOUNT_USER_DATA_CREDS_TYPE, credentials.getCredentialsType())
            am.setUserData(account, ACCOUNT_USER_DATA_COLOR, toHexColor(credentials.color, format = HexColorFormat.RGB))
            am.setUserData(account, ACCOUNT_USER_DATA_POSITION, credentials.sort_position)
            am.setUserData(account, ACCOUNT_USER_DATA_USER, JsonSerializer.serialize(credentials.account_user ?: run {
                val user = ParcelableUser()
                user.account_key = credentials.account_key
                user.key = credentials.account_key
                user.name = credentials.name
                user.screen_name = credentials.screen_name
                user.color = credentials.color
                user.profile_banner_url = credentials.profile_banner_url
                user.profile_image_url = credentials.profile_image_url
                return@run user
            }))
            am.setUserData(account, ACCOUNT_USER_DATA_EXTRAS, credentials.account_extras)
            am.setAuthToken(account, ACCOUNT_AUTH_TOKEN_TYPE, JsonSerializer.serialize(credentials.toCredentials()))
            cur.moveToNext()
        }
    } finally {
        cur.close()
    }
}

@Suppress("deprecation")
private fun ParcelableCredentials.toCredentials(): Credentials {

    fun ParcelableCredentials.applyCommonProperties(credentials: Credentials) {
        credentials.api_url_format = api_url_format
        credentials.no_version_suffix = no_version_suffix
    }

    fun ParcelableCredentials.toOAuthCredentials(): OAuthCredentials {
        val result = OAuthCredentials()
        applyCommonProperties(result)
        result.consumer_key = consumer_key
        result.consumer_secret = consumer_secret
        result.access_token = oauth_token
        result.access_token_secret = oauth_token_secret
        result.same_oauth_signing_url = same_oauth_signing_url
        return result
    }

    fun ParcelableCredentials.toBasicCredentials(): BasicCredentials {
        val result = BasicCredentials()
        applyCommonProperties(result)
        result.username = basic_auth_username
        result.password = basic_auth_password
        return result
    }

    fun ParcelableCredentials.toEmptyCredentials(): EmptyCredentials {
        val result = EmptyCredentials()
        applyCommonProperties(result)
        return result
    }

    when (auth_type) {
        AuthTypeInt.OAUTH, AuthTypeInt.XAUTH -> return toOAuthCredentials()
        AuthTypeInt.BASIC -> return toBasicCredentials()
        AuthTypeInt.TWIP_O_MODE -> return toEmptyCredentials()
    }
    throw UnsupportedOperationException()
}

@Credentials.Type
@Suppress("deprecation")
private fun ParcelableCredentials.getCredentialsType(): String {
    return AccountUtils.getCredentialsType(auth_type)
}

@Suppress("deprecation")
private val ParcelableCredentials.account_name: String
    get() = generateAccountName(screen_name, account_key.host)

fun generateAccountName(screenName: String, accountHost: String?): String {
    return UserKey(screenName, accountHost).toString()
}