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
import com.bluelinelabs.logansquare.LoganSquare
import de.vanita5.twittnuker.extension.getAccountExtras
import de.vanita5.twittnuker.extension.getCredentials
import de.vanita5.twittnuker.extension.getCredentialsType
import de.vanita5.twittnuker.model.account.cred.BasicCredentials
import de.vanita5.twittnuker.model.account.cred.Credentials
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials


fun Account.toParcelableCredentials(am: AccountManager): ParcelableCredentials {
    val credentials = ParcelableCredentials()
    writeParcelableCredentials(am, credentials)
    return credentials
}

internal fun Account.writeParcelableCredentials(am: AccountManager, credentials: ParcelableCredentials) {
    writeParcelableAccount(am, credentials)
    credentials.auth_type = when (getCredentialsType(am)) {
        Credentials.Type.OAUTH -> ParcelableCredentials.AuthTypeInt.OAUTH
        Credentials.Type.XAUTH -> ParcelableCredentials.AuthTypeInt.XAUTH
        Credentials.Type.BASIC -> ParcelableCredentials.AuthTypeInt.BASIC
        Credentials.Type.EMPTY -> ParcelableCredentials.AuthTypeInt.TWIP_O_MODE
        Credentials.Type.OAUTH2 -> ParcelableCredentials.AuthTypeInt.OAUTH2
        else -> ParcelableCredentials.AuthTypeInt.OAUTH
    }
    val extras = getAccountExtras(am)
    if (extras != null) {
        credentials.account_extras = LoganSquare.serialize(extras)
    }

    val creds = getCredentials(am)
    credentials.api_url_format = creds.api_url_format
    credentials.no_version_suffix = creds.no_version_suffix
    when (creds) {
        is OAuthCredentials -> {
            credentials.same_oauth_signing_url = creds.same_oauth_signing_url
            credentials.oauth_token = creds.access_token
            credentials.oauth_token_secret = creds.access_token_secret
            credentials.consumer_key = creds.consumer_key
            credentials.consumer_secret = creds.consumer_secret
        }
        is BasicCredentials -> {
            credentials.basic_auth_username = creds.username
            credentials.basic_auth_password = creds.password
        }
    }
}
