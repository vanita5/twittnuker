/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import de.vanita5.twittnuker.extension.newPullParser
import de.vanita5.twittnuker.extension.newSerializer
import de.vanita5.twittnuker.util.sync.FileBasedPreferencesValuesSyncAction
import de.vanita5.twittnuker.util.tempFileInputStream
import java.io.FileNotFoundException
import java.io.IOException
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
        val file = drive.getFileOrNull(name = fileName, mimeType = xmlMimeType,
                parent = commonFolderId, spaces = appDataFolderSpace,
                conflictResolver = ::resolveFilesConflict) ?: run {
            throw FileNotFoundException()
        }
        return CloseableAny(file)
    }

    override fun CloseableAny<File>.getRemoteLastModified(): Long {
        return obj.modifiedTime?.value ?: throw IOException("Modified time should not be null")
    }

    override fun CloseableAny<File>.loadFromRemote(): MutableMap<String, String> {
        val data = HashMap<String, String>()
        data.parse(files.get(obj.id).executeMediaAsInputStream().newPullParser())
        return data
    }

    override fun newSaveToRemoteSession(): GoogleDriveUploadSession<Map<String, String>> {
        return object : GoogleDriveUploadSession<Map<String, String>>(fileName, commonFolderId, xmlMimeType, drive) {
            override fun Map<String, String>.toInputStream(): InputStream {
                return tempFileInputStream(context) {
                    this.serialize(it.newSerializer(charset = Charsets.UTF_8, indent = true))
                }
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
        commonFolderId = drive.getFileOrCreate(name = commonFolderName, mimeType = folderMimeType,
                parent = appDataFolderName, spaces = appDataFolderSpace,
                conflictResolver = ::resolveFoldersConflict).id
        return true
    }
}