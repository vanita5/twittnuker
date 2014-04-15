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

package de.vanita5.twittnuker.menu;

import android.content.Context;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import de.vanita5.twittnuker.model.Account;

public class AccountActionProvider extends ActionProvider {

	private static final int MENU_GROUP = 201;

	private final Context mContext;

	private final Account[] mAccounts;

	public AccountActionProvider(final Context context) {
		super(context);
		mContext = context;
		mAccounts = Account.getAccounts(context, false, false);
	}

	@Override
	public boolean hasSubMenu() {
		return true;
	}

	@Override
	public View onCreateActionView() {
		return null;
	}

	@Override
	public void onPrepareSubMenu(final SubMenu subMenu) {
		subMenu.removeGroup(MENU_GROUP);
		for (final Account account : mAccounts) {
			final MenuItem item = subMenu.add(MENU_GROUP, (int) account.account_id, 0, account.name);
		}
		subMenu.setGroupCheckable(MENU_GROUP, true, true);
	}

}