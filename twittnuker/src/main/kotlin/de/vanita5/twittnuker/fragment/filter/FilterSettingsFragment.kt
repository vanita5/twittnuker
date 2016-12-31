package de.vanita5.twittnuker.fragment.filter

import android.os.Bundle
import de.vanita5.twittnuker.Constants.SHARED_PREFERENCES_NAME
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.fragment.BasePreferenceFragment

class FilterSettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SHARED_PREFERENCES_NAME
        addPreferencesFromResource(R.xml.preferences_filters)
    }

}