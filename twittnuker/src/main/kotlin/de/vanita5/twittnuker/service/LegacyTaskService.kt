/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.service

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.annotation.AutoRefreshType
import de.vanita5.twittnuker.constant.autoRefreshCompatibilityModeKey
import de.vanita5.twittnuker.util.TaskServiceRunner.Companion.ACTION_REFRESH_DIRECT_MESSAGES
import de.vanita5.twittnuker.util.TaskServiceRunner.Companion.ACTION_REFRESH_HOME_TIMELINE
import de.vanita5.twittnuker.util.TaskServiceRunner.Companion.ACTION_REFRESH_NOTIFICATIONS
import de.vanita5.twittnuker.util.dagger.GeneralComponent

class LegacyTaskService : BaseService() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(LOGTAG, "LegacyTaskService created")
        GeneralComponent.get(this).inject(this)
    }

    override fun onDestroy() {
        Log.d(LOGTAG, "LegacyTaskService destroyed")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(LOGTAG, "LegacyTaskService received $intent")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                !preferences[autoRefreshCompatibilityModeKey]) return START_NOT_STICKY
        val action = intent?.action ?: return START_NOT_STICKY
        taskServiceRunner.runTask(action) {
            stopSelfResult(startId)
        }
        return START_NOT_STICKY
    }


    companion object {

        fun getRefreshAction(@AutoRefreshType type: String): String? = when (type) {
            AutoRefreshType.HOME_TIMELINE -> ACTION_REFRESH_HOME_TIMELINE
            AutoRefreshType.INTERACTIONS_TIMELINE -> ACTION_REFRESH_NOTIFICATIONS
            AutoRefreshType.DIRECT_MESSAGES -> ACTION_REFRESH_DIRECT_MESSAGES
            else -> null
        }

    }
}