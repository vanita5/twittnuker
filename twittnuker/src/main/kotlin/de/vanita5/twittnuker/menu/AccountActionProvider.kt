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

package de.vanita5.twittnuker.menu

import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.view.ActionProvider
import android.view.Menu
import android.view.SubMenu
import android.view.View
import de.vanita5.twittnuker.TwittnukerConstants
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.AccountUtils

class AccountActionProvider(
        context: Context,
        var accounts: Array<AccountDetails>? = AccountUtils.getAllAccountDetails(AccountManager.get(context))
) : ActionProvider(context), TwittnukerConstants {

    var selectedAccountIds: Array<UserKey>? = null
    var isExclusive: Boolean = false

    override fun hasSubMenu(): Boolean {
        return true
    }

    override fun onCreateActionView(): View? {
        return null
    }

    override fun onPrepareSubMenu(subMenu: SubMenu) {
        subMenu.removeGroup(MENU_GROUP)
        if (accounts == null) return
        accounts?.forEachIndexed { idx, account ->
            val item = subMenu.add(MENU_GROUP, Menu.NONE, idx, account.user.name)
            val intent = Intent()
            intent.putExtra(EXTRA_ACCOUNT, account)
            item.intent = intent
        }
        subMenu.setGroupCheckable(MENU_GROUP, true, isExclusive)
        selectedAccountIds?.let {
            for (i in 0 until subMenu.size()) {
                val item = subMenu.getItem(i)
                val intent = item.intent
                val account: AccountDetails = intent.getParcelableExtra(EXTRA_ACCOUNT)
                if (it.contains(account.key)) {
                    item.isChecked = true
                }
            }
        }
    }

    companion object {

        val MENU_GROUP = 201
    }

}