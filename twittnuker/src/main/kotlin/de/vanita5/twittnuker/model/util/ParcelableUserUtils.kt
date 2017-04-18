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
import de.vanita5.twittnuker.annotation.AccountType
import org.mariotaku.ktextension.isNotNullOrEmpty
import de.vanita5.twittnuker.library.twitter.model.User
import de.vanita5.twittnuker.extension.model.api.getProfileImageOfSize
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.util.InternalTwitterContentUtils
import de.vanita5.twittnuker.util.ParseUtils
import de.vanita5.twittnuker.util.UserColorNameManager

/**
 * Processing ParcelableUser
 */
object ParcelableUserUtils {

    fun fromUser(user: User, accountKey: UserKey, accountType: String, position: Long = 0,
            profileImageSize: String = "normal"): ParcelableUser {
        return fromUserInternal(user, accountKey, accountType, position, profileImageSize)
    }

    fun fromUser(user: User, accountType: String, position: Long = 0,
            profileImageSize: String = "normal"): ParcelableUser {
        return fromUserInternal(user, null, accountType, position, profileImageSize)
    }


    private fun fromUserInternal(user: User, accountKey: UserKey?, @AccountType accountType: String?,
                                 position: Long = 0, profileImageSize: String = "normal"): ParcelableUser {
        val urlEntities = user.urlEntities
        val obj = ParcelableUser()
        obj.position = position
        obj.account_key = accountKey
        obj.key = UserKeyUtils.fromUser(user)
        obj.created_at = user.createdAt?.time ?: -1
        obj.is_protected = user.isProtected
        obj.is_verified = user.isVerified
        obj.name = user.name
        obj.screen_name = user.screenName
        obj.description_plain = user.description
        val userDescription = InternalTwitterContentUtils.formatUserDescription(user)
        if (userDescription != null) {
            obj.description_unescaped = userDescription.first
            obj.description_spans = userDescription.second
        }
        obj.location = user.location
        obj.profile_image_url = user.getProfileImageOfSize(profileImageSize)
        obj.profile_banner_url = user.profileBannerUrl
        obj.profile_background_url = user.profileBackgroundImageUrlHttps
        if (TextUtils.isEmpty(obj.profile_background_url)) {
            obj.profile_background_url = user.profileBackgroundImageUrl
        }
        obj.url = user.url
        if (obj.url != null && urlEntities.isNotNullOrEmpty()) {
            obj.url_expanded = urlEntities[0].expandedUrl
        }
        obj.is_follow_request_sent = user.isFollowRequestSent == true
        obj.followers_count = user.followersCount
        obj.friends_count = user.friendsCount
        obj.statuses_count = user.statusesCount
        obj.favorites_count = user.favouritesCount
        obj.listed_count = user.listedCount
        obj.media_count = user.mediaCount
        obj.is_following = user.isFollowing == true
        obj.background_color = parseColor(user.profileBackgroundColor)
        obj.link_color = parseColor(user.profileLinkColor)
        obj.text_color = parseColor(user.profileTextColor)
        obj.user_type = accountType
        obj.is_cache = false
        obj.is_basic = false

        val extras = ParcelableUser.Extras()
        extras.ostatus_uri = user.ostatusUri
        extras.blocking = user.isBlocking == true
        extras.blocked_by = user.isBlockedBy == true
        extras.followed_by = user.isFollowedBy == true
        extras.muting = user.isMuting == true
        extras.statusnet_profile_url = user.statusnetProfileUrl
        extras.profile_image_url_original = user.profileImageUrlOriginal ?: user.profileImageUrlLarge
        extras.pinned_status_ids = user.pinnedTweetIds
        extras.groups_count = user.groupsCount
        extras.unique_id = user.uniqueId
        obj.extras = extras
        return obj
    }


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