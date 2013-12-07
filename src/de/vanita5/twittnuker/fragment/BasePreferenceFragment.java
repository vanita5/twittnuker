package de.vanita5.twittnuker.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.vanita5.twittnuker.Constants;


public class BasePreferenceFragment extends PreferenceFragment implements Constants {

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
	}
}
