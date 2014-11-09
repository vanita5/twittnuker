package de.vanita5.twittnuker.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;

public class CreateUserMuteDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "create_user_mute";

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final ParcelableUser user = getUser();
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (user == null || twitter == null) return;
				twitter.createMuteAsync(user.account_id, user.id);
				break;
			default:
				break;
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
		final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
		final ParcelableUser user = getUser();
		if (user != null) {
			final String displayName = Utils.getDisplayName(user.name, user.screen_name);
			builder.setTitle(getString(R.string.mute_user, displayName));
			builder.setMessage(getString(R.string.mute_user_confirm_message, displayName));
		}
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}

	private ParcelableUser getUser() {
		final Bundle args = getArguments();
		if (!args.containsKey(EXTRA_USER)) return null;
		return args.getParcelable(EXTRA_USER);
	}

	public static CreateUserMuteDialogFragment show(final FragmentManager fm, final ParcelableUser user) {
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_USER, user);
		final CreateUserMuteDialogFragment f = new CreateUserMuteDialogFragment();
		f.setArguments(args);
		f.show(fm, FRAGMENT_TAG);
		return f;
	}
}