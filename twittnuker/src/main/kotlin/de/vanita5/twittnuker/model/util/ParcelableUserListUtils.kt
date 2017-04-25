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

package de.vanita5.twittnuker.model.util

import android.text.TextUtils
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.UserKey

object ParcelableUserListUtils {

    fun check(userList: ParcelableUserList, accountKey: UserKey, listId: String?,
              userKey: UserKey?, screenName: String?, listName: String?): Boolean {
        if (userList.account_key != accountKey) return false
        if (listId != null) {
            return TextUtils.equals(listId, userList.id)
        } else if (listName != null) {
            if (!TextUtils.equals(listName, userList.name)) return false
            if (userKey != null) {
                return userKey == userList.user_key
            } else if (screenName != null) {
                return TextUtils.equals(screenName, userList.user_screen_name)
            }
        }
        return false
    }
}