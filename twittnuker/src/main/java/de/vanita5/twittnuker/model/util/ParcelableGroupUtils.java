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

package de.vanita5.twittnuker.model.util;

import de.vanita5.microblog.library.statusnet.model.Group;
import de.vanita5.twittnuker.model.ParcelableGroup;
import de.vanita5.twittnuker.model.UserKey;

import java.util.Date;

public class ParcelableGroupUtils {
    private ParcelableGroupUtils() {
    }

    public static ParcelableGroup from(Group group, UserKey accountKey, int position, boolean member) {
        ParcelableGroup obj = new ParcelableGroup();
        obj.account_key = accountKey;
        obj.member = member;
        obj.position = position;
        obj.id = group.getId();
        obj.homepage = group.getHomepage();
        obj.fullname = group.getFullname();
        obj.url = group.getUrl();
        obj.description = group.getDescription();
        obj.location = group.getLocation();
        obj.created = getTime(group.getCreated());
        obj.modified = getTime(group.getModified());
        obj.admin_count = group.getAdminCount();
        obj.member_count = group.getMemberCount();
        obj.original_logo = group.getOriginalLogo();
        obj.homepage_logo = group.getHomepageLogo();
        obj.stream_logo = group.getStreamLogo();
        obj.mini_logo = group.getMiniLogo();
        obj.blocked = group.isBlocked();
        obj.id = group.getId();
        return obj;
    }

    private static long getTime(Date date) {
        if (date == null) return -1;
        return date.getTime();
    }
}