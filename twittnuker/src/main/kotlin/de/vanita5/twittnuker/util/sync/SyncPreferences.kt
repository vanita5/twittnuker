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

package de.vanita5.twittnuker.util.sync

import android.content.Context
import de.vanita5.twittnuker.TwittnukerConstants.SYNC_PREFERENCES_NAME


class SyncPreferences(val context: Context) {
    private val preferences = context.getSharedPreferences(SYNC_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun setLastSynced(type: String, timestamp: Long) {
        preferences.edit().putLong(getLastSyncedKey(type), timestamp).apply()
    }

    fun setSyncEnabled(type: String, enabled: Boolean) {
        preferences.edit().putBoolean(getSyncEnabledKey(type), enabled).apply()
    }

    fun getLastSynced(syncType: String): Long {
        return preferences.getLong(getLastSyncedKey(syncType), -1)
    }

    fun isSyncEnabled(syncType: String): Boolean {
        return preferences.getBoolean(getSyncEnabledKey(syncType), true)
    }

    companion object {

        @JvmStatic
        fun getSyncEnabledKey(type: String) = "sync_enabled_$type"

        @JvmStatic
        fun getLastSyncedKey(type: String) = "last_synced_$type"

    }
}