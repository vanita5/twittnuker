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

package de.vanita5.twittnuker.activity

import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_INTENT
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.util.StrictModeUtils
import de.vanita5.twittnuker.util.Utils

open class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            StrictModeUtils.detectAllVmPolicy()
            StrictModeUtils.detectAllThreadPolicy()
        }
        super.onCreate(savedInstanceState)
        val am = AccountManager.get(this)
        if (!Utils.checkDeviceCompatible()) {
            startActivity(Intent(this, IncompatibleAlertActivity::class.java))
        } else if (!AccountUtils.hasAccountPermission(am)) {
            Toast.makeText(this, R.string.message_toast_no_account_permission, Toast.LENGTH_SHORT).show()
        } else if (AccountUtils.hasInvalidAccount(am)) {
            val intent = Intent(this, InvalidAccountAlertActivity::class.java)
            intent.putExtra(EXTRA_INTENT, Intent(this, HomeActivity::class.java))
            startActivity(intent)
        } else {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        finish()
    }

}
