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

package de.vanita5.twittnuker.service

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.activity.SignInActivity


class AccountAuthenticatorService : Service() {

    private lateinit var authenticator: TwidereAccountAuthenticator

    override fun onCreate() {
        super.onCreate()
        authenticator = TwidereAccountAuthenticator(this)
    }

    override fun onBind(intent: Intent): IBinder {
        return authenticator.iBinder
    }

    internal class TwidereAccountAuthenticator(val context: Context) : AbstractAccountAuthenticator(context) {

        // TODO: Make SignInActivity comply with AccountAuthenticatorActivity
        override fun addAccount(response: AccountAuthenticatorResponse, accountType: String,
                                authTokenType: String?, requiredFeatures: Array<String>?,
                                options: Bundle?): Bundle {
            val intent = Intent(context, SignInActivity::class.java)
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            val result = Bundle()
            result[AccountManager.KEY_INTENT] = intent
            return result
        }

        override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account, authTokenType: String, options: Bundle?): Bundle {
            val am = AccountManager.get(context)
            val authToken = am.peekAuthToken(account, authTokenType)
            if (authToken.isNullOrEmpty()) {
                val intent = Intent(context, SignInActivity::class.java)
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
                val result = Bundle()
                result[AccountManager.KEY_INTENT] = intent
                return result
            }
            val result = Bundle()
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken)
            return result
        }

        override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account, options: Bundle?): Bundle {
            val result = Bundle()
            result[AccountManager.KEY_BOOLEAN_RESULT] = true
            return result
        }

        override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle {
            val result = Bundle()
            result[AccountManager.KEY_BOOLEAN_RESULT] = true
            return result
        }

        override fun getAuthTokenLabel(authTokenType: String): String {
            return authTokenType
        }

        override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account, features: Array<String>): Bundle {
            val result = Bundle()
            result[AccountManager.KEY_BOOLEAN_RESULT] = true
            return result
        }

        override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account,
                                       authTokenType: String, options: Bundle?): Bundle {
            val result = Bundle()
            result[AccountManager.KEY_BOOLEAN_RESULT] = true
            return result
        }
    }

}