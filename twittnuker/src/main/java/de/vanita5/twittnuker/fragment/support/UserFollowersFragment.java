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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.loader.support.CursorSupportUsersLoader;
import de.vanita5.twittnuker.loader.support.UserFollowersLoader;
import de.vanita5.twittnuker.model.AccountKey;

import static de.vanita5.twittnuker.util.DataStoreUtils.getAccountScreenName;

public class UserFollowersFragment extends CursorSupportUsersListFragment {

    private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (getActivity() == null || !isAdded() || isDetached()) return;
            final String action = intent.getAction();
            if (BROADCAST_MULTI_BLOCKSTATE_CHANGED.equals(action)) {
                final AccountKey accountKey = intent.getParcelableExtra(EXTRA_ACCOUNT_KEY);
                final String screen_name = getAccountScreenName(getActivity(), accountKey);
                final Bundle args = getArguments();
                if (args == null) return;
                if (accountKey != null && accountKey.getId() == args.getLong(EXTRA_USER_ID, -1) || screen_name != null
                        && screen_name.equalsIgnoreCase(args.getString(EXTRA_SCREEN_NAME))) {
                    removeUsers(intent.getLongArrayExtra(EXTRA_USER_IDS));
                }
            }
        }

    };

    @Override
    public CursorSupportUsersLoader onCreateUsersLoader(final Context context,
                                                        @NonNull final Bundle args, boolean fromUser) {
        final AccountKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final long userId = args.getLong(EXTRA_USER_ID, -1);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final UserFollowersLoader loader = new UserFollowersLoader(context, accountKey, userId,
                screenName, getData(), fromUser);
        loader.setCursor(getNextCursor());
        return loader;
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(BROADCAST_MULTI_BLOCKSTATE_CHANGED);
        registerReceiver(mStateReceiver, filter);

    }

    @Override
    public void onStop() {
        unregisterReceiver(mStateReceiver);
        super.onStop();
    }

}