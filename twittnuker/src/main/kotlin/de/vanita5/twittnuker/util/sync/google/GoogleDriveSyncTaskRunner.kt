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

package de.vanita5.twittnuker.util.sync.google

import android.content.Context
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import de.vanita5.twittnuker.model.sync.GoogleDriveSyncProviderInfo
import de.vanita5.twittnuker.util.TaskServiceRunner
import de.vanita5.twittnuker.util.sync.ISyncAction
import de.vanita5.twittnuker.util.sync.SyncTaskRunner
import de.vanita5.twittnuker.util.sync.UserColorsSyncProcessor


class GoogleDriveSyncTaskRunner(context: Context, val refreshToken: String) : SyncTaskRunner(context) {
    override fun onRunningTask(action: String, callback: (Boolean) -> Unit): Boolean {
        val httpTransport = NetHttpTransport.Builder().build()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val credential = GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(GoogleDriveSyncProviderInfo.WEB_CLIENT_ID, GoogleDriveSyncProviderInfo.WEB_CLIENT_SECRET)
                .build()
        credential.refreshToken = refreshToken
        val drive = Drive.Builder(httpTransport, JacksonFactory.getDefaultInstance(), credential).build()
        val syncAction: ISyncAction = when (action) {
            TaskServiceRunner.ACTION_SYNC_DRAFTS -> GoogleDriveDraftsSyncAction(context, drive)
            TaskServiceRunner.ACTION_SYNC_FILTERS -> GoogleDriveFiltersDataSyncAction(context, drive)
            TaskServiceRunner.ACTION_SYNC_USER_COLORS -> GoogleDrivePreferencesValuesSyncAction(context,
                    drive, userColorNameManager.colorPreferences, UserColorsSyncProcessor,
                    "user_colors.xml")
            else -> null
        } ?: return false
        task {
            syncAction.execute()
        }.successUi {
            callback(true)
        }.failUi {
            callback(false)
        }
        return true
    }

}