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
import android.text.TextUtils
import android.widget.CheckBox
import android.widget.EditText
import com.rengwuxian.materialedittext.MaterialEditText
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import de.vanita5.twittnuker.extension.applyOnShow
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.extension.positive
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.text.validator.UserListNameValidator
import de.vanita5.twittnuker.util.ParseUtils

class CreateUserListDialogFragment : BaseDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setView(R.layout.dialog_user_list_detail_editor)

        builder.setTitle(R.string.new_user_list)
        builder.positive(android.R.string.ok) { dialog ->
            val accountKey: UserKey = arguments.getParcelable(EXTRA_ACCOUNT_KEY)
            val editName = dialog.findViewById<EditText>(R.id.editName)!!
            val editDescription = dialog.findViewById<EditText>(R.id.editDescription)!!
            val editPublic = dialog.findViewById<CheckBox>(R.id.isPublic)!!
            val name = ParseUtils.parseString(editName.text)
            val description = ParseUtils.parseString(editDescription.text)
            val isPublic = editPublic.isChecked
            if (TextUtils.isEmpty(name)) return@positive
            twitterWrapper.createUserListAsync(accountKey, name, isPublic, description)
        }
        val dialog = builder.create()
        dialog.applyOnShow {
            applyTheme()
            val editName = dialog.findViewById<MaterialEditText>(R.id.editName)!!
            editName.addValidator(UserListNameValidator(getString(R.string.invalid_list_name)))
        }
        return dialog
    }

}