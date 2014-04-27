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

package de.vanita5.twittnuker.fragment;

import android.accounts.AccountManager;
import android.app.DialogFragment;
import android.os.Bundle;
import android.preference.Preference;

import com.google.android.gms.auth.GoogleAuthUtil;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.dialog.GoogleAccountDialog;
import de.vanita5.twittnuker.model.Account;
import de.vanita5.twittnuker.task.GetGCMTokenTask;
import de.vanita5.twittnuker.util.PushBackendHelper;

public class AccountNotificationSettingsFragment extends BaseAccountPreferenceFragment {

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Preference preference = findPreference(KEY_NOTIFICATION_LIGHT_COLOR);
		final Account account = getAccount();
		if (preference != null && account != null) {
			preference.setDefaultValue(account.color);
		}

		if (PushBackendHelper.getSavedAccountName(getActivity()) == null) {
			final android.accounts.Account[] accounts = AccountManager.get(getActivity())
					.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
			if (accounts.length == 1) {
				new GetGCMTokenTask(getActivity(), accounts[0].name, PushBackendHelper.SCOPE).execute();
			} else if (accounts.length > 1) {
				DialogFragment dialog = new GoogleAccountDialog();
				dialog.show(getFragmentManager(), "account_dialog");
			}
		}
	}

	@Override
	protected int getPreferencesResource() {
		return R.xml.settings_account_notifications;
	}

	@Override
	protected boolean getSwitchPreferenceDefault() {
		return DEFAULT_NOTIFICATION;
	}

	@Override
	protected String getSwitchPreferenceKey() {
		return KEY_NOTIFICATION;
	}

}
