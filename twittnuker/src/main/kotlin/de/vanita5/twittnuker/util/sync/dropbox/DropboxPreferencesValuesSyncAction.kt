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

package de.vanita5.twittnuker.util.sync.dropbox

import android.content.Context
import android.content.SharedPreferences
import com.dropbox.core.DbxDownloader
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadUploader
import de.vanita5.twittnuker.extension.newPullParser
import de.vanita5.twittnuker.extension.newSerializer
import de.vanita5.twittnuker.util.sync.FileBasedPreferencesValuesSyncAction
import java.util.*

internal class DropboxPreferencesValuesSyncAction(
        context: Context,
        val client: DbxClientV2,
        preferences: SharedPreferences,
        processor: Processor,
        val filePath: String
) : FileBasedPreferencesValuesSyncAction<DbxDownloader<FileMetadata>,
        DropboxUploadSession<Map<String, String>>>(context, preferences, processor) {
    override fun DbxDownloader<FileMetadata>.getRemoteLastModified(): Long {
        return result.clientModified.time
    }

    override fun DbxDownloader<FileMetadata>.loadFromRemote(): MutableMap<String, String> {
        val data = HashMap<String, String>()
        data.parse(inputStream.newPullParser())
        return data
    }

    override fun newLoadFromRemoteSession(): DbxDownloader<FileMetadata> {
        return client.newDownloader(filePath)
    }

    override fun newSaveToRemoteSession(): DropboxUploadSession<Map<String, String>> {
        return object : DropboxUploadSession<Map<String, String>>(filePath, client) {
            override fun performUpload(uploader: UploadUploader, data: Map<String, String>) {
                data.serialize(uploader.outputStream.newSerializer(charset = Charsets.UTF_8,
                        indent = true))
            }
        }
    }

    override fun DropboxUploadSession<Map<String, String>>.saveToRemote(data: MutableMap<String, String>): Boolean {
        return this.uploadData(data)
    }

    override fun DropboxUploadSession<Map<String, String>>.setRemoteLastModified(lastModified: Long) {
        this.localModifiedTime = lastModified
    }
}