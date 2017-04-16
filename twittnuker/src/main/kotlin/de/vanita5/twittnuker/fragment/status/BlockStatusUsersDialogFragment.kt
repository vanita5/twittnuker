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

package de.vanita5.twittnuker.fragment.status

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_STATUS
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.model.referencedUsers
import de.vanita5.twittnuker.fragment.BaseDialogFragment
import de.vanita5.twittnuker.fragment.CreateUserBlockDialogFragment
import de.vanita5.twittnuker.model.ParcelableStatus


class BlockStatusUsersDialogFragment : BaseDialogFragment() {

    private val status: ParcelableStatus get() = arguments.getParcelable(EXTRA_STATUS)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val referencedUsers = status.referencedUsers
        val nameFirst = preferences[nameFirstKey]
        val displayNames = referencedUsers.map {
            userColorNameManager.getDisplayName(it, nameFirst)
        }.toTypedArray()
        builder.setTitle(R.string.action_status_block_users)
        builder.setItems(displayNames) { _, which ->
            CreateUserBlockDialogFragment.show(fragmentManager, referencedUsers[which])
        }
        val dialog = builder.create()
        dialog.setOnShowListener {
            it as AlertDialog
            it.applyTheme()
        }
        return dialog
    }
}