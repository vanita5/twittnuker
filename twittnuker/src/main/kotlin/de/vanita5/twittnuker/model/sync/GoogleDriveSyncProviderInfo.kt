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

package de.vanita5.twittnuker.model.sync

import android.content.Context
import android.content.SharedPreferences
import de.vanita5.twittnuker.util.sync.SyncTaskRunner
import de.vanita5.twittnuker.util.sync.google.GoogleDriveSyncTaskRunner

class GoogleDriveSyncProviderInfo(val refreshToken: String) : SyncProviderInfo(GoogleDriveSyncProviderInfo.TYPE) {
    override fun writeToPreferences(editor: SharedPreferences.Editor) {
        editor.putString(KEY_GOOGLE_DRIVE_REFRESH_TOKEN, refreshToken)
    }

    override fun newSyncTaskRunner(context: Context): SyncTaskRunner {
        return GoogleDriveSyncTaskRunner(context, refreshToken)
    }

    companion object {
        const val TYPE = "google_drive"
        private const val KEY_GOOGLE_DRIVE_REFRESH_TOKEN = "google_drive_refresh_token"

        const val WEB_CLIENT_ID = "842794128644-7trpd51nh34jsu3cl5nij7sq5618htcn.apps.googleusercontent.com"
        const val WEB_CLIENT_SECRET = "laLBxm31A49CBZAuyJa06mUH"

        fun newInstance(preferences: SharedPreferences): GoogleDriveSyncProviderInfo? {
            val accessToken = preferences.getString(KEY_GOOGLE_DRIVE_REFRESH_TOKEN, null) ?: return null
            return GoogleDriveSyncProviderInfo(accessToken)
        }
    }
}