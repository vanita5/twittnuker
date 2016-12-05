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

package de.vanita5.twittnuker.util

import android.accounts.Account
import android.accounts.AccountManager
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.annotation.ColorInt
import com.bluelinelabs.logansquare.LoganSquare
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.annotation.AuthTypeInt
import de.vanita5.twittnuker.model.ParcelableCredentials
import de.vanita5.twittnuker.model.ParcelableCredentialsCursorIndices
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.account.cred.BasicCredentials
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.account.cred.EmptyCredentials
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.Accounts
import de.vanita5.twittnuker.util.support.AccountManagerSupport
import java.util.*

fun migrateAccounts(am: AccountManager, db: SQLiteDatabase) {
    am.getAccountsByType(ACCOUNT_TYPE).map { account ->
        AccountManagerSupport.removeAccount(am, account, null, null, null)
    }

    val cur = db.query(Accounts.TABLE_NAME, Accounts.COLUMNS, null, null, null, null, null) ?: return
    try {
        val indices = ParcelableCredentialsCursorIndices(cur)
        cur.moveToFirst()
        while (!cur.isAfterLast) {
            val credentials = indices.newObject(cur)
            val account = Account(credentials.account_name, ACCOUNT_TYPE)
            val userdata = Bundle()
            userdata.putString(ACCOUNT_USER_DATA_KEY, credentials.account_key.toString())
            userdata.putString(ACCOUNT_USER_DATA_TYPE, credentials.account_type)
            userdata.putString(ACCOUNT_USER_DATA_CREDS_TYPE, credentials.getCredentialsType())
            userdata.putString(ACCOUNT_USER_DATA_ACTIVATED, credentials.is_activated.toString())
            userdata.putString(ACCOUNT_USER_DATA_USER, LoganSquare.serialize(credentials.account_user))
            userdata.putString(ACCOUNT_USER_DATA_EXTRAS, credentials.account_extras)
            userdata.putString(ACCOUNT_USER_DATA_COLOR, toHexColor(credentials.color))
            am.addAccountExplicitly(account, null, userdata)
            am.setAuthToken(account, ACCOUNT_AUTH_TOKEN_TYPE, LoganSquare.serialize(credentials.toCredentials()))
            cur.moveToNext()
        }
    } finally {
        cur.close()
    }
}

fun toHexColor(@ColorInt color: Int) = String.format(Locale.ROOT, "#%6X", color)

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
    get() = UserKey(screen_name, account_key.host).toString()