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

package de.vanita5.twittnuker.loader;

import android.content.Context;

import de.vanita5.twittnuker.api.statusnet.model.Group;
import de.vanita5.twittnuker.api.twitter.Twitter;
import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.model.ParcelableGroup;
import de.vanita5.twittnuker.model.UserKey;

import java.util.List;

public class UserGroupsLoader extends BaseGroupsLoader {

    private final UserKey mUserKey;
    private final String mScreenName;

    public UserGroupsLoader(final Context context, final UserKey accountKey, final UserKey userKey,
                            final String screenName, final List<ParcelableGroup> data) {
        super(context, accountKey, 0, data);
        mUserKey = userKey;
        mScreenName = screenName;
    }

    @Override
    public ResponseList<Group> getGroups(final Twitter twitter) throws TwitterException {
        if (twitter == null) return null;
        if (mUserKey != null) {
            return twitter.getGroups(mUserKey.getId());
        } else if (mScreenName != null) {
            return twitter.getGroups(mScreenName);
        }
        return null;
    }

    @Override
    protected boolean isMember(final Group list) {
        return true;
    }
}