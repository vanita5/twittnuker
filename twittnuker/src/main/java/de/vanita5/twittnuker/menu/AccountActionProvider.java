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

package de.vanita5.twittnuker.menu;

import android.content.Context;
import android.content.Intent;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.apache.commons.lang3.ArrayUtils;
import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.util.ParcelableAccountUtils;

public class AccountActionProvider extends ActionProvider implements TwittnukerConstants {

    public static final int MENU_GROUP = 201;

    private ParcelableAccount[] mAccounts;

    private AccountKey[] mAccountIds;
    private boolean mExclusive;

    public AccountActionProvider(final Context context, final ParcelableAccount[] accounts) {
        super(context);
        setAccounts(accounts);
    }

    public AccountActionProvider(final Context context) {
        this(context, ParcelableAccountUtils.getAccounts(context, false, false));
    }


    @Override
    public boolean hasSubMenu() {
        return true;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    public void setAccounts(ParcelableAccount[] accounts) {
        mAccounts = accounts;
    }

    @Override
    public void onPrepareSubMenu(final SubMenu subMenu) {
        subMenu.removeGroup(MENU_GROUP);
        if (mAccounts == null) return;
        for (int i = 0, j = mAccounts.length; i < j; i++) {
            final ParcelableAccount account = mAccounts[i];
            final MenuItem item = subMenu.add(MENU_GROUP, Menu.NONE, i, account.name);
            final Intent intent = new Intent();
            intent.putExtra(EXTRA_ACCOUNT, account);
            item.setIntent(intent);
        }
        subMenu.setGroupCheckable(MENU_GROUP, true, mExclusive);
        if (mAccountIds == null) return;
        for (int i = 0, j = subMenu.size(); i < j; i++) {
            final MenuItem item = subMenu.getItem(i);
            final Intent intent = item.getIntent();
            final ParcelableAccount account = intent.getParcelableExtra(EXTRA_ACCOUNT);
            if (ArrayUtils.contains(mAccountIds, account.account_key)) {
                item.setChecked(true);
            }
        }
    }

    public boolean isExclusive() {
        return mExclusive;
    }


    public void setExclusive(boolean exclusive) {
        mExclusive = exclusive;
    }

    public void setSelectedAccountIds(final AccountKey... accountIds) {
        mAccountIds = accountIds;
    }

}