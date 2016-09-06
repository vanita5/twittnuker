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

import de.vanita5.twittnuker.library.twitter.model.Activity
import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.ParcelableUser
import de.vanita5.twittnuker.model.UserKey

import java.util.*

/**
 * Processing ParcelableActivity
 */
object ParcelableActivityUtils {

    /**
     * @param activity        Activity for processing
     * *
     * @param filteredUserIds Those ids will be removed from source_ids.
     * *
     * @param followingOnly   Limit following users in sources
     * *
     * @return true if source ids changed, false otherwise
     */
    fun initAfterFilteredSourceIds(activity: ParcelableActivity, filteredUserIds: Array<UserKey>,
                                   followingOnly: Boolean): Boolean {
        if (activity.sources == null) return false
        if (activity.after_filtered_source_ids != null) return false
        if (followingOnly || filteredUserIds.isNotEmpty()) {
            val list = ArrayList<UserKey>()
            for (user in activity.sources) {
                if (followingOnly && !user.is_following) {
                    continue
                }

                if (!filteredUserIds.contains(user.key)) {
                    list.add(user.key)
                }
            }
            activity.after_filtered_source_ids = list.toTypedArray()
            return true
        } else {
            activity.after_filtered_source_ids = activity.source_ids
            return false
        }
    }

    fun getAfterFilteredSources(activity: ParcelableActivity): Array<ParcelableUser> {
        if (activity.after_filtered_sources != null) return activity.after_filtered_sources
        if (activity.after_filtered_source_ids == null || activity.sources.size == activity.after_filtered_source_ids.size) {
            return activity.sources
        }
        val result = Array<ParcelableUser>(activity.after_filtered_source_ids.size) { idx ->
            return@Array activity.sources.find { it.key == activity.after_filtered_source_ids[idx] }!!
        }
        activity.after_filtered_sources = result
        return result
    }

    fun fromActivity(activity: Activity,
                     accountKey: UserKey,
                     isGap: Boolean): ParcelableActivity {
        val result = ParcelableActivity()
        result.account_key = accountKey
        result.timestamp = activity.createdAt.time
        result.action = activity.action
        result.max_sort_position = activity.maxSortPosition
        result.min_sort_position = activity.minSortPosition
        result.max_position = activity.maxPosition
        result.min_position = activity.minPosition
        result.sources = ParcelableUserUtils.fromUsers(activity.sources, accountKey)
        result.target_users = ParcelableUserUtils.fromUsers(activity.targetUsers, accountKey)
        result.target_user_lists = ParcelableUserListUtils.fromUserLists(activity.targetUserLists, accountKey)
        result.target_statuses = ParcelableStatusUtils.fromStatuses(activity.targetStatuses, accountKey)
        result.target_object_statuses = ParcelableStatusUtils.fromStatuses(activity.targetObjectStatuses, accountKey)
        result.target_object_user_lists = ParcelableUserListUtils.fromUserLists(activity.targetObjectUserLists, accountKey)
        result.target_object_users = ParcelableUserUtils.fromUsers(activity.targetObjectUsers, accountKey)
        result.has_following_source = activity.sources.fold(false) { folded, item ->
            if (item.isFollowing) {
                return@fold true
            }
            return@fold false
        }
        if (result.sources != null) {
            result.source_ids = arrayOfNulls<UserKey>(result.sources.size)
            for (i in result.sources.indices) {
                result.source_ids[i] = result.sources[i].key
            }
        }
        result.is_gap = isGap
        return result
    }


}