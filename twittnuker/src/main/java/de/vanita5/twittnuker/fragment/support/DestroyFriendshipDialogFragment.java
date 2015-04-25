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

package de.vanita5.twittnuker.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.constant.SharedPreferenceConstants;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.UserColorNameManager;

public class DestroyFriendshipDialogFragment extends BaseSupportDialogFragment implements
		DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "destroy_friendship";

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final ParcelableUser user = getUser();
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (user == null || twitter == null) return;
				twitter.destroyFriendshipAsync(user.account_id, user.id);
				break;
			default:
				break;
		}
	}

    @NonNull
	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        final Context wrapped = ThemeUtils.getDialogThemedContext(activity);
        final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
		final ParcelableUser user = getUser();
		if (user != null) {
            final UserColorNameManager manager = UserColorNameManager.getInstance(activity);
            final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(activity,
                    SharedPreferencesWrapper.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE,
                    SharedPreferenceConstants.class);
            final boolean nameFirst = prefs.getBoolean(KEY_NAME_FIRST);
            final String displayName = manager.getDisplayName(user, nameFirst, true);
            builder.setTitle(getString(R.string.unfollow_user, displayName));
            builder.setMessage(getString(R.string.unfollow_user_confirm_message, displayName));
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

	public static DestroyFriendshipDialogFragment show(final FragmentManager fm, final ParcelableUser user) {
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_USER, user);
		final DestroyFriendshipDialogFragment f = new DestroyFriendshipDialogFragment();
		f.setArguments(args);
		f.show(fm, FRAGMENT_TAG);
		return f;
	}
}