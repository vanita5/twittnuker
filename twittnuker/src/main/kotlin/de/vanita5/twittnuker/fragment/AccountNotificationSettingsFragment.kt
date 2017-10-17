/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.os.Bundle
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.*
import de.vanita5.twittnuker.preference.notification.AccountNotificationChannelsPreference

class AccountNotificationSettingsFragment : BaseAccountPreferenceFragment() {

    override val preferencesResource: Int
        get() = R.xml.preferences_account_notifications

    override val switchPreferenceDefault: Boolean
        get() = DEFAULT_NOTIFICATION

    override val switchPreferenceKey: String?
        get() = KEY_NOTIFICATION

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val account = this.account
        findPreference(KEY_NOTIFICATION_LIGHT_COLOR)?.let {
            if (account != null) {
                it.setDefaultValue(account.color)
            }
        }
        (findPreference("notification_channels") as? AccountNotificationChannelsPreference)?.account = account
    }

}