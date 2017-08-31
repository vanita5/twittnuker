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

package de.vanita5.twittnuker.util.notification

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.constant.nameFirstKey
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.notification.NotificationChannelSpec
import de.vanita5.twittnuker.util.dagger.DependencyHolder

object NotificationChannelsManager {
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        NotificationChannelCreatorImpl.createChannels(context)
    }

    fun createAccountGroup(context: Context, account: AccountDetails) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        NotificationChannelCreatorImpl.createAccountGroup(context, account)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private object NotificationChannelCreatorImpl {

        fun createChannels(context: Context) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val values = NotificationChannelSpec.values()
            nm.notificationChannels.filterNot { channel ->
                values.any { channel.id == it.id }
            }.forEach {
                nm.deleteNotificationChannel(it.id)
            }
            for (spec in values) {
                val channel = NotificationChannel(spec.id, context.getString(spec.nameRes), spec.importance)
                if (spec.descriptionRes != 0) {
                    channel.description = context.getString(spec.descriptionRes)
                }
                channel.setShowBadge(spec.showBadge)
                nm.createNotificationChannel(channel)
            }
        }

        fun createAccountGroup(context: Context, account: AccountDetails) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val holder = DependencyHolder.get(context)
            val pref = holder.preferences
            val ucnm = holder.userColorNameManager
            val group = NotificationChannelGroup(account.key.toString(),
                    ucnm.getDisplayName(account.user, pref[nameFirstKey]))
            nm.createNotificationChannelGroup(group)
        }
    }
}