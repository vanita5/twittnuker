package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import de.vanita5.twittnuker.TwidereConstants;
import de.vanita5.twittnuker.fragment.AccountRefreshSettingsFragment;
import de.vanita5.twittnuker.model.Account;


public class AutoRefreshAccountsListPreference extends AccountsListPreference implements TwidereConstants {

	public AutoRefreshAccountsListPreference(final Context context) {
		super(context);
	}

	public AutoRefreshAccountsListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public AutoRefreshAccountsListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void setupPreference(final AccountItemPreference preference, final Account account) {
		preference.setFragment(AccountRefreshSettingsFragment.class.getName());
		final Bundle args = preference.getExtras();
		args.putParcelable(EXTRA_ACCOUNT, account);
	}

}
