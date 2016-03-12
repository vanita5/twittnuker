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

package de.vanita5.twittnuker.model.util;

import org.apache.commons.collections.primitives.ArrayLongList;
import org.apache.commons.lang3.ArrayUtils;
import de.vanita5.twittnuker.api.twitter.model.Activity;
import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableActivity;
import de.vanita5.twittnuker.model.ParcelableUser;

public class ParcelableActivityUtils {

    /**
     * @param activity        Activity for processing
     * @param filteredUserIds Those ids will be removed from source_ids.
     * @param followingOnly   Limit following users in sources
     * @return true if source ids changed, false otherwise
     */
    public static boolean initAfterFilteredSourceIds(ParcelableActivity activity, long[] filteredUserIds,
                                                     boolean followingOnly) {
        if (activity.after_filtered_source_ids != null) return false;
        if (followingOnly || !ArrayUtils.isEmpty(filteredUserIds)) {
            ArrayLongList list = new ArrayLongList();
            for (ParcelableUser user : activity.sources) {
                if (followingOnly && !user.is_following) {
                    continue;
                }
                if (!ArrayUtils.contains(filteredUserIds, user.id)) {
                    list.add(user.id);
                }
            }
            activity.after_filtered_source_ids = list.toArray();
            return true;
        } else {
            activity.after_filtered_source_ids = activity.source_ids;
            return false;
        }
    }

    public static ParcelableUser[] getAfterFilteredSources(ParcelableActivity activity) {
        if (activity.after_filtered_sources != null) return activity.after_filtered_sources;
        if (activity.after_filtered_source_ids == null || activity.sources.length == activity.after_filtered_source_ids.length) {
            return activity.sources;
        }
        ParcelableUser[] result = new ParcelableUser[activity.after_filtered_source_ids.length];
        for (int i = 0; i < activity.after_filtered_source_ids.length; i++) {
            for (ParcelableUser user : activity.sources) {
                if (user.id == activity.after_filtered_source_ids[i]) {
                    result[i] = user;
                }
            }
        }
        return activity.after_filtered_sources = result;
    }

    public static ParcelableActivity fromActivity(final Activity activity, final AccountKey accountKey,
                                                  final boolean isGap) {
        ParcelableActivity result = new ParcelableActivity();
        result.account_key = accountKey.getId();
        result.account_host = accountKey.getHost();
        result.timestamp = activity.getCreatedAt().getTime();
        result.action = activity.getAction();
        result.max_position = activity.getMaxPosition();
        result.min_position = activity.getMinPosition();
        result.sources = ParcelableUserUtils.fromUsers(activity.getSources(), accountKey);
        result.target_users = ParcelableUserUtils.fromUsers(activity.getTargetUsers(), accountKey);
        result.target_user_lists = ParcelableUserListUtils.fromUserLists(activity.getTargetUserLists(), accountKey);
        result.target_statuses = ParcelableStatusUtils.fromStatuses(activity.getTargetStatuses(), accountKey);
        result.target_object_statuses = ParcelableStatusUtils.fromStatuses(activity.getTargetObjectStatuses(), accountKey);
        result.target_object_user_lists = ParcelableUserListUtils.fromUserLists(activity.getTargetObjectUserLists(), accountKey);
        result.target_object_users = ParcelableUserUtils.fromUsers(activity.getTargetObjectUsers(), accountKey);
        if (result.sources != null) {
            result.source_ids = new long[result.sources.length];
            for (int i = 0; i < result.sources.length; i++) {
                result.source_ids[i] = result.sources[i].id;
            }
        }
        result.is_gap = isGap;
        return result;
    }


}