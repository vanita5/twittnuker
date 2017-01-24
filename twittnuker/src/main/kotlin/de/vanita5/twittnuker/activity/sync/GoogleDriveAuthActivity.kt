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

package de.vanita5.twittnuker.activity.sync

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.AccountPicker
import com.google.api.services.drive.DriveScopes
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.set
import de.vanita5.twittnuker.activity.BaseActivity
import de.vanita5.twittnuker.constant.dataSyncProviderInfoKey
import de.vanita5.twittnuker.model.sync.GoogleDriveSyncProviderInfo


class GoogleDriveAuthActivity : BaseActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = AccountPicker.newChooseAccountIntent(null, null,
                arrayOf(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE), false, null, null, null, null)
        startActivityForResult(intent, REQUEST_CODE_CHOOSE_ACCOUNT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_CHOOSE_ACCOUNT -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    val type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
                    val account = Account(name, type)
                    task {
                        return@task GoogleAuthUtil.getToken(this, account, "oauth2:${DriveScopes.DRIVE_APPDATA}")
                    }.successUi { accessToken ->
                        preferences[dataSyncProviderInfoKey] = GoogleDriveSyncProviderInfo(accessToken)
                        finish()
                    }.fail { ex ->
                        if (ex is UserRecoverableAuthException) {
                            startActivityForResult(ex.intent, REQUEST_CODE_AUTH_ERROR_RECOVER)
                        } else {
                            finish()
                        }
                    }
                }
            }
            REQUEST_CODE_AUTH_ERROR_RECOVER -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    val type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
                    val token = data.getStringExtra(AccountManager.KEY_AUTHTOKEN)
                    preferences[dataSyncProviderInfoKey] = GoogleDriveSyncProviderInfo(token)
                }
                finish()
            }
        }
    }

    companion object {

        private const val REQUEST_CODE_CHOOSE_ACCOUNT: Int = 101
        private const val REQUEST_CODE_AUTH_ERROR_RECOVER: Int = 102
    }
}