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
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_account_selector.*
import org.mariotaku.ktextension.toTypedArray
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.adapter.AccountDetailsAdapter
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.app.TwittnukerApplication
import de.vanita5.twittnuker.extension.model.isOAuth
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.util.DataStoreUtils

class AccountSelectorActivity : BaseActivity(), OnItemClickListener {

    private lateinit var adapter: AccountDetailsAdapter

    private val onlyIncludeKeys: Array<UserKey>?
        get() {
            return intent.getParcelableArrayExtra(EXTRA_ACCOUNT_KEYS)?.toTypedArray(UserKey.CREATOR)
        }

    private val isOAuthOnly: Boolean
        get() {
            return intent.getBooleanExtra(EXTRA_OAUTH_ONLY, false)
        }

    /**
     * If not null, account selector will only show accounts matched this host.
     */
    private val accountHost: String?
        get() = intent.getStringExtra(EXTRA_ACCOUNT_HOST)

    private val accountType: String?
        get() = intent.getStringExtra(EXTRA_ACCOUNT_TYPE)

    private val isSelectNoneAllowed: Boolean
        get() = intent.getBooleanExtra(EXTRA_ALLOW_SELECT_NONE, false)

    private val isSingleSelection: Boolean
        get() = intent.getBooleanExtra(EXTRA_SINGLE_SELECTION, true)

    /**
     * True if you want account picked automatically if there are only one match.
     */
    private val isSelectOnlyItemAutomatically: Boolean
        get() = intent.getBooleanExtra(EXTRA_SELECT_ONLY_ITEM_AUTOMATICALLY, false)

    private val startIntent: Intent?
        get() {
            val startIntent = intent.getParcelableExtra<Intent>(EXTRA_START_INTENT)
            startIntent?.setExtrasClassLoader(TwittnukerApplication::class.java.classLoader)
            return startIntent
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_selector)
        DataStoreUtils.prepareDatabase(this)
        adapter = AccountDetailsAdapter(this, Glide.with(this)).apply {
            switchEnabled = !isSingleSelection
            sortEnabled = false
            val am = AccountManager.get(context)
            val allAccountDetails = AccountUtils.getAllAccountDetails(am, AccountUtils.getAccounts(am), false)
            val extraKeys = onlyIncludeKeys
            val oauthOnly = isOAuthOnly
            val accountHost = accountHost
            val accountType = accountType
            addAll(allAccountDetails.filter {
                if (extraKeys != null) {
                    return@filter extraKeys.contains(it.key)
                }
                if (oauthOnly && !it.isOAuth) {
                    return@filter false
                }
                if (USER_TYPE_TWITTER_COM == accountHost) {
                    if (it.key.host != null && it.type != AccountType.TWITTER) return@filter false
                } else if (accountHost != null) {
                    if (accountHost != it.key.host) return@filter false
                }
                if (accountType != null) {
                    if (accountType != it.type) return@filter false
                }
                return@filter true
            })
        }
        accountsList.choiceMode = if (isSingleSelection) ListView.CHOICE_MODE_NONE else ListView.CHOICE_MODE_MULTIPLE
        if (isSingleSelection) {
            accountsList.onItemClickListener = this
        }
        selectAccountButtons.visibility = if (isSingleSelection) View.GONE else View.VISIBLE
        accountsList.adapter = adapter
        if (adapter.count == 1 && isSelectOnlyItemAutomatically) {
            selectSingleAccount(0)
        }
        confirmSelection.setOnClickListener {
            val checkedIds = accountsList.checkedItemIds
            if (checkedIds.isEmpty() && !isSelectNoneAllowed) {
                Toast.makeText(this, R.string.message_toast_no_account_selected, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val data = Intent()
            data.putExtra(EXTRA_IDS, checkedIds)
            data.putExtra(EXTRA_EXTRAS, intent.getBundleExtra(EXTRA_EXTRAS))
            setResult(Activity.RESULT_OK, data)
            finish()
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        selectSingleAccount(position)
    }

    private fun selectSingleAccount(position: Int) {
        val account = adapter.getItem(position)
        val data = Intent()
        data.putExtra(EXTRA_ID, account.key.id)
        data.putExtra(EXTRA_ACCOUNT_KEY, account.key)
        data.putExtra(EXTRA_EXTRAS, intent.getBundleExtra(EXTRA_EXTRAS))

        val startIntent = startIntent
        if (startIntent != null) {
            startIntent.putExtra(EXTRA_ACCOUNT_KEY, account.key)
            startActivity(startIntent)
        }

        setResult(Activity.RESULT_OK, data)
        finish()
    }

}