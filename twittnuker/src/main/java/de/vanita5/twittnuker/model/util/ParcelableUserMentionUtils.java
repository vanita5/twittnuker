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

import de.vanita5.twittnuker.library.twitter.model.User;
import de.vanita5.twittnuker.library.twitter.model.UserMentionEntity;
import de.vanita5.twittnuker.model.ParcelableUserMention;
import de.vanita5.twittnuker.model.UserKey;

public class ParcelableUserMentionUtils {
    private ParcelableUserMentionUtils() {
    }

    public static ParcelableUserMention fromMentionEntity(final User user,
                                                          final UserMentionEntity entity) {
        ParcelableUserMention obj = new ParcelableUserMention();
        obj.key = new UserKey(entity.getId(), UserKeyUtils.getUserHost(user));
        obj.name = entity.getName();
        obj.screen_name = entity.getScreenName();
        return obj;
    }

    public static ParcelableUserMention[] fromUserMentionEntities(final User user,
                                                                  final UserMentionEntity[] entities) {
        if (entities == null) return null;
        final ParcelableUserMention[] mentions = new ParcelableUserMention[entities.length];
        for (int i = 0, j = entities.length; i < j; i++) {
            mentions[i] = fromMentionEntity(user, entities[i]);
        }
        return mentions;
    }
}