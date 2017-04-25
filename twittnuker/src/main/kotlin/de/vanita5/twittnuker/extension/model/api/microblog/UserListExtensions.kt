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

package de.vanita5.twittnuker.extension.model.api.microblog

import de.vanita5.twittnuker.library.twitter.model.UserList
import de.vanita5.twittnuker.extension.model.api.getProfileImageOfSize
import de.vanita5.twittnuker.model.ParcelableUserList
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.util.UserKeyUtils

fun UserList.toParcelable(accountKey: UserKey, position: Long = 0, isFollowing: Boolean = false,
                          profileImageSize: String = "normal"): ParcelableUserList {
    val obj = ParcelableUserList()
    val user = user
    obj.position = position
    obj.account_key = accountKey
    obj.id = id
    obj.is_public = UserList.Mode.PUBLIC == mode
    obj.is_following = isFollowing
    obj.name = name
    obj.description = description
    obj.user_key = UserKeyUtils.fromUser(user)
    obj.user_name = user.name
    obj.user_screen_name = user.screenName
    obj.user_profile_image_url = user.getProfileImageOfSize(profileImageSize)
    obj.members_count = memberCount
    obj.subscribers_count = subscriberCount
    return obj
}