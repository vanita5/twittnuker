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

package de.vanita5.twittnuker.model.util

import android.text.TextUtils
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.util.ParseUtils
import de.vanita5.twittnuker.util.UserColorNameManager

/**
 * Processing ParcelableUser
 */
object ParcelableUserUtils {

    fun parseColor(colorString: String?): Int {
        if (colorString == null) return 0
        var str: String = colorString
        if (!str.startsWith("#")) {
            str = "#" + str
        }
        return ParseUtils.parseColor(str, 0)
    }

    fun updateExtraInformation(user: ParcelableUser, account: AccountDetails,
            manager: UserColorNameManager) {
        user.account_color = account.color
        user.color = manager.getUserColor(user.key)
    }

    fun getExpandedDescription(user: ParcelableUser): String {
        if (TextUtils.isEmpty(user.description_unescaped)) {
            return user.description_plain
        }
        if (user.description_spans != null) {
            // TODO expand description
        }
        return user.description_unescaped
    }
}