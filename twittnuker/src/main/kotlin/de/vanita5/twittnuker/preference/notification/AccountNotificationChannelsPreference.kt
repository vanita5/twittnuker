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
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.preference.notification

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager
import android.util.AttributeSet
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.extension.model.getDescription
import de.vanita5.twittnuker.extension.model.getName
import de.vanita5.twittnuker.extension.model.notificationChannelId
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.notification.NotificationChannelSpec
import de.vanita5.twittnuker.preference.TintedPreferenceCategory

@TargetApi(Build.VERSION_CODES.O)
class AccountNotificationChannelsPreference(context: Context, attrs: AttributeSet? = null) : TintedPreferenceCategory(context, attrs) {
    var account: AccountDetails? = null

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager?) {
        super.onAttachedToHierarchy(preferenceManager)
        initItems()
    }

    private fun initItems() {
        removeAll()
        val specs = NotificationChannelSpec.values().filter { it.grouped }.sortedBy { it.getName(context) }
        specs.forEach { spec ->
            val preference = Preference(context)
            preference.title = spec.getName(context)
            preference.summary = spec.getDescription(context)
            preference.setOnPreferenceClickListener lambda@ {
                val account = this.account ?: return@lambda true
                val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
                        .putExtra(Settings.EXTRA_CHANNEL_ID, account.key.notificationChannelId(spec.id))
                context.startActivity(intent)
                return@lambda true
            }
            addPreference(preference)
        }
    }

}