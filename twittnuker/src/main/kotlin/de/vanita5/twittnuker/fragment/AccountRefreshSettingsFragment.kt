/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.fragment

import android.content.SharedPreferences
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.DEFAULT_AUTO_REFRESH
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.KEY_AUTO_REFRESH
import de.vanita5.twittnuker.util.Utils

class AccountRefreshSettingsFragment : BaseAccountPreferenceFragment() {

    override val preferencesResource: Int
        get() = R.xml.preferences_account_refresh

    override val switchPreferenceDefault: Boolean
        get() = DEFAULT_AUTO_REFRESH

    override val switchPreferenceKey: String?
        get() = KEY_AUTO_REFRESH

    override fun onSharedPreferenceChanged(preferences: SharedPreferences, key: String) {
        if (KEY_AUTO_REFRESH == key) {
            Utils.startRefreshServiceIfNeeded(activity)
        }
    }
}