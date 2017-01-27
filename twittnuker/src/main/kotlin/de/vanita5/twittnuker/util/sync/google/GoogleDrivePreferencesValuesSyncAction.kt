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
import android.content.SharedPreferences
import com.dropbox.core.DbxDownloader
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadUploader
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import de.vanita5.twittnuker.extension.model.serialize
import de.vanita5.twittnuker.extension.newPullParser
import de.vanita5.twittnuker.extension.newSerializer
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.util.io.DirectByteArrayOutputStream
import de.vanita5.twittnuker.util.sync.FileBasedPreferencesValuesSyncAction
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

internal class GoogleDrivePreferencesValuesSyncAction(
        context: Context,
        val drive: Drive,
        preferences: SharedPreferences,
        processor: Processor,
        val fileName: String
) : FileBasedPreferencesValuesSyncAction<CloseableAny<File>,
        GoogleDriveUploadSession<Map<String, String>>>(context, preferences, processor) {

    private lateinit var commonFolderId: String

    private val files = drive.files()

    override fun newLoadFromRemoteSession(): CloseableAny<File> {
        val file = files.getOrNull(fileName, xmlMimeType, commonFolderId) ?: throw FileNotFoundException()
        return CloseableAny(file)
    }

    override fun CloseableAny<File>.getRemoteLastModified(): Long {
        return (obj.modifiedTime ?: obj.createdTime)?.value ?: 0
    }

    override fun CloseableAny<File>.loadFromRemote(): MutableMap<String, String> {
        val data = HashMap<String, String>()
        data.parse(files.get(obj.id).executeAsInputStream().newPullParser())
        return data
    }

    override fun newSaveToRemoteSession(): GoogleDriveUploadSession<Map<String, String>> {
        return object : GoogleDriveUploadSession<Map<String, String>>(fileName, commonFolderId, xmlMimeType, files) {
            override fun Map<String, String>.toInputStream(): InputStream {
                val os = DirectByteArrayOutputStream()
                this.serialize(os.newSerializer(charset = Charsets.UTF_8, indent = true))
                return os.inputStream(true)
            }
        }
    }

    override fun GoogleDriveUploadSession<Map<String, String>>.saveToRemote(data: MutableMap<String, String>): Boolean {
        return this.uploadData(data)
    }


    override fun GoogleDriveUploadSession<Map<String, String>>.setRemoteLastModified(lastModified: Long) {
        this.localModifiedTime = lastModified
    }

    override fun setup(): Boolean {
        commonFolderId = files.getOrCreate("Common", folderMimeType).id
        return true
    }
}