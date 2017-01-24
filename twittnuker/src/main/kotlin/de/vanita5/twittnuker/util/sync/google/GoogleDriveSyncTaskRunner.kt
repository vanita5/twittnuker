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

import android.content.ComponentCallbacks
import android.content.Context
import android.os.Bundle
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.util.TaskServiceRunner
import de.vanita5.twittnuker.util.sync.ISyncAction
import de.vanita5.twittnuker.util.sync.SyncTaskRunner
import de.vanita5.twittnuker.util.sync.UserColorsSyncProcessor
import de.vanita5.twittnuker.util.sync.dropbox.DropboxDraftsSyncAction
import de.vanita5.twittnuker.util.sync.dropbox.DropboxFiltersDataSyncAction
import de.vanita5.twittnuker.util.sync.dropbox.DropboxPreferencesValuesSyncAction
import java.net.ConnectException


class GoogleDriveSyncTaskRunner(context: Context) : SyncTaskRunner(context) {
    override fun onRunningTask(action: String, callback: (Boolean) -> Unit): Boolean {
        val client = GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build()
        val syncAction: ISyncAction = when (action) {
            TaskServiceRunner.ACTION_SYNC_DRAFTS -> GoogleDriveDraftsSyncAction(context, client)
            else -> null
        } ?: return false
        task {
            val connResult = client.blockingConnect()
            if (!connResult.isSuccess) {
                throw ConnectException()
            }
            syncAction.execute()
        }.successUi {
            callback(true)
        }.failUi {
            callback(false)
        }.always {
            client.disconnect()
        }
        return true
    }

}