/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.loader.support.IDsUsersLoader;
import de.vanita5.twittnuker.loader.support.IncomingFriendshipsLoader;
import de.vanita5.twittnuker.model.Account;
import de.vanita5.twittnuker.model.Account.AccountWithCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;


public class IncomingFriendshipsFragment extends CursorSupportUsersListFragment {

	@Override
	public IDsUsersLoader newLoaderInstance(final Context context, final Bundle args) {
		if (args == null) return null;
		final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
		return new IncomingFriendshipsLoader(context, accountId, getNextCursor(), getData());
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
	    switch (item.getItemId()) {
			case MENU_ACCEPT: {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				final ParcelableUser user = getSelectedUser();
				if (twitter == null || user == null) return false;
					twitter.acceptFriendshipAsync(user.account_id, user.id);
					break;
			}
			case MENU_DENY: {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				final ParcelableUser user = getSelectedUser();
				if (twitter == null || user == null) return false;
					twitter.denyFriendshipAsync(user.account_id, user.id);
					break;
			}
		}
		return super.onMenuItemClick(item);
	}

	@Override
	protected void onPrepareItemMenu(final Menu menu) {
	    final AccountWithCredentials account = Account.getAccountWithCredentials(getActivity(), getAccountId());
	    if (AccountWithCredentials.isOfficialCredentials(getActivity(), account)) {
			final MenuInflater inflater = new MenuInflater(getActivity());
			inflater.inflate(R.menu.action_incoming_friendship, menu);
		}
	}

}
