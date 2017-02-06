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

package de.vanita5.twittnuker.util

import android.view.View
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.view.holder.TwoLineWithIconViewHolder

fun TwoLineWithIconViewHolder.display(
        user: ParcelableUser,
        mediaLoader: MediaLoaderWrapper,
        userColorNameManager: UserColorNameManager,
        displayProfileImage: Boolean
) {
    text1.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            Utils.getUserTypeIconRes(user.is_verified, user.is_protected), 0)
    text1.text = user.name
    text2.text = String.format("@%s", user.screen_name)
    icon.visibility = if (displayProfileImage) View.VISIBLE else View.GONE
    if (displayProfileImage) {
        mediaLoader.displayProfileImage(icon, user)
    } else {
        mediaLoader.cancelDisplayTask(icon)
    }
}