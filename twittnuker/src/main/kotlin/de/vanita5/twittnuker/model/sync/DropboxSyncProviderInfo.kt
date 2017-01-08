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

package de.vanita5.twittnuker.model.sync

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import de.vanita5.twittnuker.service.DropboxDataSyncService
import de.vanita5.twittnuker.util.sync.SyncController

class DropboxSyncProviderInfo(val authToken: String) : SyncProviderInfo(DropboxSyncProviderInfo.TYPE) {
    override fun writeToPreferences(editor: SharedPreferences.Editor) {
        editor.putString(KEY_DROPBOX_AUTH_TOKEN, authToken)
    }

    override fun newSyncController(context: Context): SyncController {
        return DropboxSyncController(context)
    }

    companion object {
        const val TYPE = "dropbox"

        private const val KEY_DROPBOX_AUTH_TOKEN = "dropbox_auth_token"
        fun newInstance(preferences: SharedPreferences): DropboxSyncProviderInfo? {
            val authToken = preferences.getString(KEY_DROPBOX_AUTH_TOKEN, null) ?: return null
            return DropboxSyncProviderInfo(authToken)
        }
    }

    class DropboxSyncController(val context: Context) : SyncController() {
        override fun cleanupSyncCache() {

        }

        override fun performSync() {
            context.startService(Intent(context, DropboxDataSyncService::class.java))
        }

    }

}