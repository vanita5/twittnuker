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
import android.content.Intent
import android.content.SharedPreferences
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.sync.DropboxAuthStarterActivity
import de.vanita5.twittnuker.activity.sync.GoogleDriveAuthActivity
import de.vanita5.twittnuker.model.sync.GoogleDriveSyncProviderInfo
import de.vanita5.twittnuker.model.sync.DropboxSyncProviderInfo
import de.vanita5.twittnuker.model.sync.SyncProviderEntry
import de.vanita5.twittnuker.model.sync.SyncProviderInfo
import java.util.*

class NonFreeSyncProviderInfoFactory : SyncProviderInfoFactory() {
    override fun getInfoForType(type: String, preferences: SharedPreferences): SyncProviderInfo? {
        return when (type) {
            DropboxSyncProviderInfo.TYPE -> DropboxSyncProviderInfo.newInstance(preferences)
            GoogleDriveSyncProviderInfo.TYPE -> GoogleDriveSyncProviderInfo.newInstance(preferences)
            else -> null
        }
    }

    override fun getSupportedProviders(context: Context): List<SyncProviderEntry> {
        val list = ArrayList<SyncProviderEntry>()
        list.add(SyncProviderEntry(DropboxSyncProviderInfo.TYPE,
                        context.getString(R.string.sync_provider_name_dropbox),
                Intent(context, DropboxAuthStarterActivity::class.java)))
        if (BuildConfig.DEBUG) {
            list.add(SyncProviderEntry(GoogleDriveSyncProviderInfo.TYPE,
                        context.getString(R.string.sync_provider_name_google_drive),
                    Intent(context, GoogleDriveAuthActivity::class.java)))
        }
        return list
    }
}
