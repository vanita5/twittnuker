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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.model.Account;
import de.vanita5.twittnuker.task.AsyncTask;
import de.vanita5.twittnuker.util.ImageLoaderWrapper;

import java.util.List;

public abstract class AccountsListPreference extends PreferenceCategory {

	public AccountsListPreference(final Context context) {
		super(context);
	}

	public AccountsListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public AccountsListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setAccountsData(final List<Account> accounts) {
		removeAll();
		for (final Account account : accounts) {
			final AccountItemPreference preference = new AccountItemPreference(getContext(), account);
			setupPreference(preference, account);
			addPreference(preference);
		}
	}

	@Override
	protected void onAttachedToHierarchy(final PreferenceManager preferenceManager) {
		super.onAttachedToHierarchy(preferenceManager);
		new LoadAccountsTask(this).execute();
	}

	protected abstract void setupPreference(AccountItemPreference preference, Account account);

	public static final class AccountItemPreference extends Preference implements ImageLoadingListener {
		private final Account mAccount;

		public AccountItemPreference(final Context context, final Account account) {
			super(context);
			mAccount = account;
		}

		@Override
		public void onLoadingCancelled(final String imageUri, final View view) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		public void onLoadingComplete(final String imageUri, final View view, final Bitmap loadedImage) {
			setIcon(new BitmapDrawable(getContext().getResources(), loadedImage));
		}

		@Override
		public void onLoadingFailed(final String imageUri, final View view, final FailReason failReason) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		public void onLoadingProgressChanged(final String imageUri, final View view, final int current, final int total) {

		}

		@Override
		public void onLoadingStarted(final String imageUri, final View view) {
			setIcon(R.drawable.ic_profile_image_default);
		}

		@Override
		protected void onAttachedToHierarchy(final PreferenceManager preferenceManager) {
			super.onAttachedToHierarchy(preferenceManager);
			setTitle(mAccount.name);
			setSummary(String.format("@%s", mAccount.screen_name));
			setIcon(R.drawable.ic_profile_image_default);
			final TwittnukerApplication app = TwittnukerApplication.getInstance(getContext());
			final ImageLoaderWrapper loader = app.getImageLoaderWrapper();
			loader.loadProfileImage(mAccount.profile_image_url, this);
		}

		@Override
		protected void onBindView(final View view) {
			super.onBindView(view);
		}

	}

	private static class LoadAccountsTask extends AsyncTask<Void, Void, List<Account>> {

		private final AccountsListPreference mPreference;

		public LoadAccountsTask(final AccountsListPreference preference) {
			mPreference = preference;
		}

		@Override
		protected List<Account> doInBackground(final Void... params) {
			return Account.getAccounts(mPreference.getContext(), false);
		}

		@Override
		protected void onPostExecute(final List<Account> result) {
			mPreference.setAccountsData(result);
		}

	}

}
