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

package de.vanita5.twittnuker.util.refresh

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.support.v4.util.ArrayMap
import org.mariotaku.kpreferences.KPreferences
import de.vanita5.twittnuker.annotation.AutoRefreshType
import de.vanita5.twittnuker.constant.refreshIntervalKey
import de.vanita5.twittnuker.service.LegacyTaskService
import java.util.concurrent.TimeUnit

class LegacyAutoRefreshController(
        context: Context,
        kPreferences: KPreferences
) : AutoRefreshController(context, kPreferences) {

    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val pendingIntents: ArrayMap<String, PendingIntent> = ArrayMap()

    init {
        AutoRefreshType.ALL.forEach { type ->
            val action = LegacyTaskService.getRefreshAction(type) ?: return@forEach
            val intent = Intent(context, LegacyTaskService::class.java)
            intent.action = action
            pendingIntents[type] = PendingIntent.getService(context, 0, intent, 0)
        }
    }

    override fun appStarted() {
        rescheduleAll()
    }

    override fun unschedule(type: String) {
        val pendingIntent = pendingIntents[type] ?: return
        alarmManager.cancel(pendingIntent)
    }

    override fun schedule(type: String) {
        val pendingIntent = pendingIntents[type] ?: return
        val interval = TimeUnit.MINUTES.toMillis(kPreferences[refreshIntervalKey])
        if (interval > 0) {
            val triggerAt = SystemClock.elapsedRealtime() + interval
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, interval, pendingIntent)
        }
    }

}