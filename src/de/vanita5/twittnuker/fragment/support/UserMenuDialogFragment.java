package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import static de.vanita5.twittnuker.util.Utils.addIntentToMenu;

import de.vanita5.twittnuker.activity.support.MenuDialogFragment;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.util.Utils;

public class UserMenuDialogFragment extends MenuDialogFragment {

    @Override
    protected void onCreateMenu(final MenuInflater inflater, final Menu menu) {
        final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        final Bundle args = getArguments();
        final ParcelableUser user = args.getParcelable(EXTRA_USER);
        onPrepareItemMenu(menu, user);
        final boolean longclickToOpenMenu = prefs.getBoolean(KEY_LONG_CLICK_TO_OPEN_MENU, false);
        Utils.setMenuItemAvailability(menu, MENU_MULTI_SELECT, longclickToOpenMenu);
    }

    protected void onPrepareItemMenu(final Menu menu, final ParcelableUser user) {

    }

}