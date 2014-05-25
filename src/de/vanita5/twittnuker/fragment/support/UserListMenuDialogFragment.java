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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.MenuDialogFragment;
import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.util.Utils;

import static de.vanita5.twittnuker.util.Utils.addIntentToMenu;

public class UserListMenuDialogFragment extends MenuDialogFragment {

	@Override
	protected void onCreateMenu(final MenuInflater inflater, final Menu menu) {
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final Bundle args = getArguments();
		final ParcelableUserList user = args.getParcelable(EXTRA_USER_LIST);
		inflater.inflate(R.menu.action_user_list, menu);
		onPrepareItemMenu(menu, user);
		final Intent extensionsIntent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST);
		final Bundle extensionsExtras = new Bundle();
		extensionsExtras.putParcelable(EXTRA_USER_LIST, user);
		extensionsIntent.putExtras(extensionsExtras);
		addIntentToMenu(getThemedContext(), menu, extensionsIntent);
		final boolean longclickToOpenMenu = prefs.getBoolean(KEY_LONG_CLICK_TO_OPEN_MENU, false);
		Utils.setMenuItemAvailability(menu, MENU_MULTI_SELECT, longclickToOpenMenu);
	}

	protected void onPrepareItemMenu(final Menu menu, final ParcelableUserList userList) {
		if (userList == null) return;
		final boolean isMyList = userList.user_id == userList.account_id;
		Utils.setMenuItemAvailability(menu, MENU_ADD, isMyList);
		Utils.setMenuItemAvailability(menu, MENU_DELETE, isMyList);
	}

}