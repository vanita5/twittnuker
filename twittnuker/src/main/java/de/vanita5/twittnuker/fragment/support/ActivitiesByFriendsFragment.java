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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.adapter.ParcelableActivitiesAdapter;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities;
import de.vanita5.twittnuker.util.ErrorInfoStore;

public class ActivitiesByFriendsFragment extends CursorActivitiesFragment {

    @Override
    public boolean getActivities(long[] accountIds, long[] maxIds, long[] sinceIds) {
        mTwitterWrapper.getActivitiesByFriendsAsync(accountIds, maxIds, sinceIds);
        return false;
    }

    @NonNull
    @Override
    protected String getErrorInfoKey() {
        return ErrorInfoStore.KEY_ACTIVITIES_BY_FRIENDS;
    }

    @Override
    public Uri getContentUri() {
        return Activities.ByFriends.CONTENT_URI;
    }

    @Override
    protected int getNotificationType() {
        return 0;
    }

    @Override
    protected boolean isFilterEnabled() {
        return false;
    }

    @Override
    protected void updateRefreshState() {

    }

    @NonNull
    @Override
    protected ParcelableActivitiesAdapter onCreateAdapter(Context context, boolean compact) {
        final ParcelableActivitiesAdapter adapter = new ParcelableActivitiesAdapter(context, compact,
                true);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            final Bundle extras = arguments.getBundle(EXTRA_EXTRAS);
            if (extras != null) {
                adapter.setFollowingOnly(extras.getBoolean(EXTRA_MY_FOLLOWING_ONLY));
                adapter.setMentionsOnly(extras.getBoolean(EXTRA_MENTIONS_ONLY));
            }
        }
        return adapter;
    }


    @Override
    public boolean isRefreshing() {
        return false;
    }

}