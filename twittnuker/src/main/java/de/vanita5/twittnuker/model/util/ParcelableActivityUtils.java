/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import de.vanita5.twittnuker.model.ParcelableActivity;
import de.vanita5.twittnuker.model.ParcelableUser;

public class ParcelableActivityUtils {
    public static void getAfterFilteredSourceIds(ParcelableActivity activity, long[] filteredUserIds, boolean followingOnly) {
        if (activity.after_filtered_source_ids != null) return;
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
        } else {
            activity.after_filtered_source_ids = activity.source_ids;
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
}