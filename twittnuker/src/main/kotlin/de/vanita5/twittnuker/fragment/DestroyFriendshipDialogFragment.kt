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
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.KEY_NAME_FIRST
import de.vanita5.twittnuker.extension.applyTheme
import de.vanita5.twittnuker.model.ParcelableUser

class DestroyFriendshipDialogFragment : BaseDialogFragment(), DialogInterface.OnClickListener {

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> {
                val user = user ?: return
                twitterWrapper.destroyFriendshipAsync(user.account_key, user.key)
            }
            else -> {
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val nameFirst = preferences.getBoolean(KEY_NAME_FIRST)
        val displayName = userColorNameManager.getDisplayName(user, nameFirst)
        builder.setTitle(getString(R.string.unfollow_user, displayName))
        builder.setMessage(getString(R.string.unfollow_user_confirm_message, displayName))
        builder.setPositiveButton(android.R.string.ok, this)
        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            it as AlertDialog
            it.applyTheme()
        }
        return dialog
    }

    private val user: ParcelableUser?
        get() {
            val args = arguments
            if (!args.containsKey(IntentConstants.EXTRA_USER)) return null
            return args.getParcelable<ParcelableUser>(IntentConstants.EXTRA_USER)
        }

    companion object {

        val FRAGMENT_TAG = "destroy_friendship"

        fun show(fm: FragmentManager, user: ParcelableUser): DestroyFriendshipDialogFragment {
            val args = Bundle()
            args.putParcelable(IntentConstants.EXTRA_USER, user)
            val f = DestroyFriendshipDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}