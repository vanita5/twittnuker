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

package de.vanita5.twittnuker.activity.content

import android.content.Intent
import android.os.Bundle
import de.vanita5.twittnuker.TwittnukerConstants.REQUEST_SELECT_ACCOUNT
import de.vanita5.twittnuker.activity.AccountSelectorActivity
import de.vanita5.twittnuker.activity.BaseActivity
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.UserKey

abstract class AbsStatusDialogActivity : BaseActivity() {

    private val statusId: String?
        get() = intent.getStringExtra(EXTRA_STATUS_ID)

    private val accountKey: UserKey?
        get() = intent.getParcelableExtra(EXTRA_ACCOUNT_KEY)

    private val accountHost: String?
        get() = intent.getStringExtra(EXTRA_ACCOUNT_HOST)

    private val status: ParcelableStatus?
        get() = intent.getParcelableExtra(EXTRA_STATUS)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val statusId = this.statusId ?: run {
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            val accountKey = this.accountKey
            if (accountKey != null) {
                showDialogFragment(accountKey, statusId, status)
            } else {
                val intent = Intent(this, AccountSelectorActivity::class.java)
                intent.putExtra(EXTRA_SINGLE_SELECTION, true)
                intent.putExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, true)
                intent.putExtra(EXTRA_ACCOUNT_HOST, accountHost)
                startActivityForResult(intent, REQUEST_SELECT_ACCOUNT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SELECT_ACCOUNT -> {
                if (resultCode == RESULT_OK && data != null) {
                    val statusId = this.statusId ?: run {
                        setResult(RESULT_CANCELED)
                        finish()
                        return
                    }
                    val accountKey = data.getParcelableExtra<UserKey>(EXTRA_ACCOUNT_KEY)
                    showDialogFragment(accountKey, statusId, status)
                    return
                }
            }
        }
        finish()
    }

    protected abstract fun showDialogFragment(accountKey: UserKey, statusId: String,
                                              status: ParcelableStatus?)
}