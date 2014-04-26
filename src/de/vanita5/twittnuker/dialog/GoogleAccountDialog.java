package de.vanita5.twittnuker.dialog;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthUtil;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.task.GetGCMTokenTask;
import de.vanita5.twittnuker.util.PushBackendHelper;

public class GoogleAccountDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle args) {
		Builder builder = new Builder(getActivity());
		builder.setTitle(R.string.select_account);
		final Account[] accounts = AccountManager.get(getActivity())
				.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		if (accounts != null) {
			final int size = accounts.length;
			String[] names = new String[size];

			for (int i = 0; i < size; i++) {
				names[i] = accounts[i].name;
			}
			builder.setItems(names, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					accountSelected(accounts[i]);
				}
			});
		}
		return builder.create();
	}

	public void accountSelected(Account account) {
		if (account != null) {
			new GetGCMTokenTask(getActivity(), account.name, PushBackendHelper.SCOPE).execute();
		}
	}
}
