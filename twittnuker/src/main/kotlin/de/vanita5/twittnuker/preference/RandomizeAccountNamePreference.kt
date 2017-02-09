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

package de.vanita5.twittnuker.preference

import android.accounts.AccountManager
import android.content.Context
import android.content.res.TypedArray
import android.support.v4.util.ArraySet
import android.support.v7.preference.DialogPreference
import android.support.v7.preference.PreferenceDialogFragmentCompat
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceViewHolder
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import org.mariotaku.ktextension.Bundle
import org.mariotaku.ktextension.set
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.extension.model.getAccountKey
import de.vanita5.twittnuker.extension.model.getAccountUser
import de.vanita5.twittnuker.extension.model.renameTwidereAccount
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.preference.iface.IDialogPreference
import de.vanita5.twittnuker.util.generateAccountName
import java.util.*

class RandomizeAccountNamePreference @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = R.attr.switchPreferenceCompatStyle,
        defStyleRes: Int = 0
) : DialogPreference(context, attrs, defStyleAttr, defStyleRes), IDialogPreference {

    init {
        dialogTitle = title
        dialogMessage = context.getString(R.string.preference_randomize_account_rename_accounts_confirm)
        positiveButtonText = context.getString(android.R.string.ok)
        negativeButtonText = context.getString(android.R.string.cancel)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val switchView = holder.findViewById(android.support.v7.preference.R.id.switchWidget) as SwitchCompat
        switchView.isChecked = getPersistedBoolean(false)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getBoolean(index, false)
    }

    override fun onClick() {
        persistBoolean(!getPersistedBoolean(false));
        notifyChanged()
        super.onClick()
    }

    override fun displayDialog(fragment: PreferenceFragmentCompat) {
        val df = RenameAccountsConfirmDialogFragment.newInstance(key, getPersistedBoolean(false))
        df.setTargetFragment(fragment, 0)
        df.show(fragment.childFragmentManager, key)
    }

    class RenameAccountsConfirmDialogFragment : PreferenceDialogFragmentCompat() {

        override fun onDialogClosed(positiveResult: Boolean) {
            val am = AccountManager.get(context)
            val enabled = arguments.getBoolean(ARG_VALUE)
            if (enabled) {
                val usedNames = ArraySet<String>()
                AccountUtils.getAccounts(am).forEach { oldAccount ->
                    var newName: String
                    do {
                        newName = UUID.randomUUID().toString()
                    } while (usedNames.contains(newName))
                    am.renameTwidereAccount(oldAccount, newName)
                    usedNames.add(newName)
                }
            } else {
                AccountUtils.getAccounts(am).forEach { oldAccount ->
                    val accountKey = oldAccount.getAccountKey(am)
                    val accountUser = oldAccount.getAccountUser(am)
                    val newName = generateAccountName(accountUser.screen_name, accountKey.host)
                    am.renameTwidereAccount(oldAccount, newName)
                }
            }
        }

        companion object {
            const val ARG_VALUE = "value"
            fun newInstance(key: String, value: Boolean): RenameAccountsConfirmDialogFragment {
                val df = RenameAccountsConfirmDialogFragment()
                df.arguments = Bundle {
                    this[ARG_KEY] = key
                    this[ARG_VALUE] = value
                }
                return df
            }
        }
    }
}