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
import android.os.Bundle;
import android.support.v4.content.Loader;

import de.vanita5.twittnuker.loader.support.UserMentionsLoader;
import de.vanita5.twittnuker.model.ParcelableStatus;

import java.util.List;

public class UserMentionsFragment extends StatusesSearchFragment {

    @Override
    protected Loader<List<ParcelableStatus>> onCreateStatusesLoader(final Context context,
                                                                    final Bundle args,
                                                                    final boolean fromUser) {
        if (args == null) return null;
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final long maxId = args.getLong(EXTRA_MAX_ID, -1);
        final long sinceId = args.getLong(EXTRA_SINCE_ID, -1);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean makeGap = args.getBoolean(EXTRA_MAKE_GAP, true);
        return new UserMentionsLoader(getActivity(), accountId, screenName, maxId, sinceId,
                getAdapterData(), getSavedStatusesFileArgs(), tabPosition, fromUser, makeGap
        );
    }

    @Override
    protected String[] getSavedStatusesFileArgs() {
        final Bundle args = getArguments();
        if (args == null) return null;
        final long account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
        final String screen_name = args.getString(EXTRA_SCREEN_NAME);
        return new String[]{AUTHORITY_USER_MENTIONS, "account" + account_id, "screen_name" + screen_name};
    }

}