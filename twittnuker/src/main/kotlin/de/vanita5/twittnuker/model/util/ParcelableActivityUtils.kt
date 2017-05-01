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

import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.ParcelableLiteUser
import de.vanita5.twittnuker.model.UserKey

/**
 * Processing ParcelableActivity
 */
object ParcelableActivityUtils {

    /**
     * @param activity        Activity for processing
     * *
     * @param filteredUserKeys Those ids will be removed from source_ids.
     * *
     * @param followingOnly   Limit following users in sources
     * *
     * @return true if source ids changed, false otherwise
     */
    fun initAfterFilteredSourceIds(activity: ParcelableActivity, filteredUserKeys: Array<UserKey>,
                                   followingOnly: Boolean): Boolean {
        if (activity.sources == null) return false
        if (activity.after_filtered_source_keys != null) return false
        if (followingOnly || filteredUserKeys.isNotEmpty()) {
            val list = activity.sources.filter { user ->
                if (followingOnly && !user.is_following) {
                    return@filter false
                }

                if (!filteredUserKeys.contains(user.key)) {
                    return@filter true
                }
                return@filter false
            }.map { it.key }
            activity.after_filtered_source_keys = list.toTypedArray()
            return true
        } else {
            activity.after_filtered_source_keys = activity.source_keys
            return false
        }
    }

    fun getAfterFilteredSources(activity: ParcelableActivity): Array<ParcelableLiteUser> {
        val sources = activity.sources_lite ?: return emptyArray()
        val afterFilteredKeys = activity.after_filtered_source_keys?.takeIf {
            it.size != sources.size
        } ?: return sources
        return sources.filter { it.key in afterFilteredKeys }.toTypedArray()

    }


}