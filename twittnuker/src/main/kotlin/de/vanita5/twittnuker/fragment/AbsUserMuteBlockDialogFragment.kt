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

package de.vanita5.twittnuker.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.CheckBox
import android.widget.TextView
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER
import de.vanita5.twittnuker.model.ParcelableUser

abstract class AbsUserMuteBlockDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    private val user: ParcelableUser by lazy { arguments.getParcelable<ParcelableUser>(EXTRA_USER) }

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val filterEverywhere = ((dialog as Dialog).findViewById(R.id.filterEverywhereToggle) as CheckBox).isChecked
                performUserAction(user, filterEverywhere)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(getTitle(user))
        builder.setView(R.layout.dialog_block_mute_filter_user_confirm)
        builder.setPositiveButton(getPositiveButtonTitle(user), this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val confirmMessageView = dialog.findViewById(R.id.confirmMessage) as TextView
            val filterEverywhereHelp = dialog.findViewById(R.id.filterEverywhereHelp)!!
            filterEverywhereHelp.setOnClickListener {
                MessageDialogFragment.show(childFragmentManager, title = getString(R.string.filter_everywhere),
                        message = getString(R.string.filter_everywhere_description), tag = "filter_everywhere_help")
            }
            confirmMessageView.text = getMessage(user)
        }
        return dialog
    }

    abstract fun performUserAction(user: ParcelableUser, filterEverywhere: Boolean)

    protected abstract fun getTitle(user: ParcelableUser): String
    protected abstract fun getMessage(user: ParcelableUser): String
    protected open fun getPositiveButtonTitle(user: ParcelableUser): String = getString(android.R.string.ok)
}