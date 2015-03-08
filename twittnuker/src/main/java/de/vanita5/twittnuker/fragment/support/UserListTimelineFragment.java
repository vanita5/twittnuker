/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;

import de.vanita5.twittnuker.loader.support.UserListTimelineLoader;
import de.vanita5.twittnuker.model.ParcelableStatus;

import java.util.List;

public class UserListTimelineFragment extends ParcelableStatusesFragment {

	@Override
    public Loader<List<ParcelableStatus>> onCreateStatusesLoader(final Context context,
                                                                 final Bundle args,
                                                                 final boolean fromUser) {
        setRefreshing(true);
		if (args == null) return null;
        final long listId = args.getLong(EXTRA_LIST_ID, -1);
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long maxId = args.getLong(EXTRA_MAX_ID, -1);
        final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final String listName = args.getString(EXTRA_LIST_NAME);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        return new UserListTimelineLoader(getActivity(), accountId, listId, userId, screenName,
                listName, sinceId, maxId, getAdapterData(), getSavedStatusesFileArgs(), tabPosition, fromUser);
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        if (args == null) return null;
        final long listId = args.getLong(EXTRA_LIST_ID, -1);
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final String listName = args.getString(EXTRA_LIST_NAME);
        return new String[]{AUTHORITY_USER_LIST_TIMELINE, "account" + accountId, "list_id" + listId,
                "list_name" + listName, "user_id" + userId, "screen_name" + screenName};
	}

}
