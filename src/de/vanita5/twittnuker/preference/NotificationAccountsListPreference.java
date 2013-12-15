package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.fragment.AccountNotificationSettingsFragment;
import de.vanita5.twittnuker.model.Account;


public class NotificationAccountsListPreference extends AccountsListPreference implements TwittnukerConstants {

	public NotificationAccountsListPreference(final Context context) {
		super(context);
	}

	public NotificationAccountsListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public NotificationAccountsListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void setupPreference(final AccountItemPreference preference, final Account account) {
		preference.setFragment(AccountNotificationSettingsFragment.class.getName());
		final Bundle args = preference.getExtras();
		args.putParcelable(EXTRA_ACCOUNT, account);
	}

}
