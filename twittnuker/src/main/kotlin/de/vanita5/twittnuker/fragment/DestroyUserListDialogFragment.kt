/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER_LIST
import de.vanita5.twittnuker.extension.applyOnShow
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.model.ParcelableUserList

class DestroyUserListDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val userList = userList
                val twitter = twitterWrapper
                twitter.destroyUserListAsync(userList.account_key, userList.id)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = activity
        val builder = AlertDialog.Builder(context)
        val userList = userList
        builder.setTitle(getString(R.string.delete_user_list, userList.name))
        builder.setMessage(getString(R.string.delete_user_list_confirm_message, userList.name))
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.applyOnShow { applyTheme() }
        return dialog
    }

    private val userList: ParcelableUserList
        get() = arguments.getParcelable<ParcelableUserList>(EXTRA_USER_LIST)

    companion object {

        private const val FRAGMENT_TAG = "destroy_user_list"

        fun show(fm: FragmentManager, userList: ParcelableUserList): DestroyUserListDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_USER_LIST, userList)
            val f = DestroyUserListDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}