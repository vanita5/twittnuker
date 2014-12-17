package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableAccount.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;

public class IncomingFriendshipsMenuDialogFragment extends UserMenuDialogFragment {

    @Override
    protected void onPrepareItemMenu(final Menu menu, final ParcelableUser user) {
		final Context context = getThemedContext();
        final ParcelableCredentials account = ParcelableAccount.getCredentials(context, user.account_id);
        if (ParcelableCredentials.isOfficialCredentials(context, account)) {
            final MenuInflater inflater = new MenuInflater(context);
            inflater.inflate(R.menu.action_incoming_friendship, menu);
        }
    }

}