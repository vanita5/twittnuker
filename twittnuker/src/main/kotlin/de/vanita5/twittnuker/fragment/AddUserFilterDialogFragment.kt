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

import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.widget.Toast
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.message.FriendshipTaskEvent
import de.vanita5.twittnuker.util.DataStoreUtils

class AddUserFilterDialogFragment : AbsUserMuteBlockDialogFragment() {
    override fun getMessage(user: ParcelableUser): String {
        return getString(R.string.filter_user_confirm_message, userColorNameManager.getDisplayName(user, kPreferences[nameFirstKey]))
    }

    override fun getTitle(user: ParcelableUser): String {
        return getString(R.string.add_to_filter)
    }

    override fun performUserAction(user: ParcelableUser, filterEverywhere: Boolean) {
        DataStoreUtils.addToFilter(context, listOf(user), filterEverywhere)
        bus.post(FriendshipTaskEvent(FriendshipTaskEvent.Action.FILTER, user.account_key, user.key).apply {
            isFinished = true
            isSucceeded = true
        })
        Toast.makeText(context, R.string.message_toast_added_to_filter, Toast.LENGTH_SHORT).show()
    }

    companion object {

        const val FRAGMENT_TAG = "add_user_filter"

        fun show(fm: FragmentManager, user: ParcelableUser): AddUserFilterDialogFragment {
            val args = Bundle()
            args.putParcelable(EXTRA_USER, user)
            val f = AddUserFilterDialogFragment()
            f.arguments = args
            f.show(fm, FRAGMENT_TAG)
            return f
        }
    }
}