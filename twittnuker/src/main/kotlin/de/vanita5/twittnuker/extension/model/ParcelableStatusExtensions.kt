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

package de.vanita5.twittnuker.extension.model

import org.mariotaku.ktextension.addAllTo
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.ParcelableUserMention
import de.vanita5.twittnuker.model.UserKey

val ParcelableStatus.media_type: Int
    get() = media?.firstOrNull()?.type ?: 0

val ParcelableStatus.user: ParcelableUser
    get() = ParcelableUser(account_key, user_key, user_name, user_screen_name, user_profile_image_url)

val ParcelableStatus.referencedUsers: Array<ParcelableUser>
    get() {
        val resultList = mutableSetOf(user)
        if (quoted_user_key != null) {
            resultList.add(ParcelableUser(account_key, quoted_user_key, quoted_user_name,
                    quoted_user_screen_name, quoted_user_profile_image))
        }
        if (retweeted_by_user_key != null) {
            resultList.add(ParcelableUser(account_key, retweeted_by_user_key, retweeted_by_user_name,
                    retweeted_by_user_screen_name, retweeted_by_user_profile_image))
        }
        mentions?.forEach { mention ->
            resultList.add(ParcelableUser(account_key, mention.key, mention.name,
                    mention.screen_name, null))
        }
        return resultList.toTypedArray()
    }

val ParcelableStatus.replyMentions: Array<ParcelableUserMention>
    get() {
        val result = ArrayList<ParcelableUserMention>()
        result.add(parcelableUserMention(user_key, user_name, user_screen_name))
        if (is_retweet) retweeted_by_user_key?.let { key ->
            result.add(parcelableUserMention(key, retweeted_by_user_name,
                    retweeted_by_user_screen_name))
        }
        mentions?.addAllTo(result)
        return result.toTypedArray()
    }

private fun parcelableUserMention(key: UserKey, name: String, screenName: String) = ParcelableUserMention().also {
    it.key = key
    it.name = name
    it.screen_name = screenName
}