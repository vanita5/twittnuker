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

package de.vanita5.twittnuker.menu;

import android.content.Context;
import android.content.Intent;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.model.ParcelableAccount;

public class AccountActionProvider extends ActionProvider implements TwittnukerConstants {

	public static final int MENU_GROUP = 201;

	private final ParcelableAccount[] mAccounts;

	private long mAccountId;

	public AccountActionProvider(final Context context) {
		super(context);
		mAccounts = ParcelableAccount.getAccounts(context, false, false);
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
		for (final ParcelableAccount account : mAccounts) {
			final MenuItem item = subMenu.add(MENU_GROUP, Menu.NONE, 0, account.name);
			final Intent intent = new Intent();
			intent.putExtra(EXTRA_ACCOUNT, account);
			item.setIntent(intent);
		}
		subMenu.setGroupCheckable(MENU_GROUP, true, true);
		for (int i = 0, j = subMenu.size(); i < j; i++) {
			final MenuItem item = subMenu.getItem(i);
			final Intent intent = item.getIntent();
			final ParcelableAccount account = intent.getParcelableExtra(EXTRA_ACCOUNT);
			if (account.account_id == mAccountId) {
				item.setChecked(true);
			}
		}
	}

	public void setAccountId(final long accountId) {
		mAccountId = accountId;
	}

}