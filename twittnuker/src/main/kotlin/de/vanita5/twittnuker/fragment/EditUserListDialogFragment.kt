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

package de.vanita5.twittnuker.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.dialog_user_list_detail_editor.*
import org.mariotaku.ktextension.string
import de.vanita5.microblog.library.twitter.model.UserList
import de.vanita5.microblog.library.twitter.model.UserListUpdate
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.onShow
import de.vanita5.twittnuker.extension.positive
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.text.validator.UserListNameValidator

class EditUserListDialogFragment : BaseDialogFragment() {

    private val accountKey by lazy { arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY) }
    private val listId: String by lazy { arguments.getString(EXTRA_LIST_ID) }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_user_list_detail_editor)
        builder.setTitle(R.string.title_user_list)
        builder.positive(android.R.string.ok, this::onPositiveClick)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.onShow { dialog ->
            dialog.applyTheme()
            dialog.editName.addValidator(UserListNameValidator(getString(R.string.invalid_list_name)))
            if (savedInstanceState == null) {
                dialog.editName.setText(arguments.getString(EXTRA_LIST_NAME))
                dialog.editDescription.setText(arguments.getString(EXTRA_DESCRIPTION))
                dialog.isPublic.isChecked = arguments.getBoolean(EXTRA_IS_PUBLIC, true)
            }
        }
        return dialog
    }

    private fun onPositiveClick(dialog: Dialog) {
        val name = dialog.editName.string?.takeIf(String::isNotEmpty) ?: return
        val description = dialog.editDescription.string
        val isPublic = dialog.isPublic.isChecked
        val update = UserListUpdate()
        update.setMode(if (isPublic) UserList.Mode.PUBLIC else UserList.Mode.PRIVATE)
        update.setName(name)
        update.setDescription(description)
        twitterWrapper.updateUserListDetails(accountKey, listId, update)
    }

}