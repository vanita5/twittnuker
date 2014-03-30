package de.vanita5.twittnuker.fragment.support;

import android.view.Menu;
import android.view.MenuInflater;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.model.Account;
import de.vanita5.twittnuker.model.Account.AccountWithCredentials;
import de.vanita5.twittnuker.model.ParcelableUser;

public class IncomingFriendshipsMenuDialogFragment extends UserMenuDialogFragment {

    @Override
    protected void onPrepareItemMenu(final Menu menu, final ParcelableUser user) {
        final AccountWithCredentials account = Account.getAccountWithCredentials(getActivity(), user.account_id);
        if (AccountWithCredentials.isOfficialCredentials(getActivity(), account)) {
            final MenuInflater inflater = new MenuInflater(getActivity());
            inflater.inflate(R.menu.action_incoming_friendship, menu);
        }
    }

}