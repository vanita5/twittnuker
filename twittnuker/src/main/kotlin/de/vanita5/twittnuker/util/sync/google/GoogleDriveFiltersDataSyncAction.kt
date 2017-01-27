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
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import de.vanita5.twittnuker.extension.model.initFields
import de.vanita5.twittnuker.extension.model.parse
import de.vanita5.twittnuker.extension.model.serialize
import de.vanita5.twittnuker.extension.newPullParser
import de.vanita5.twittnuker.extension.newSerializer
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.util.io.DirectByteArrayOutputStream
import de.vanita5.twittnuker.util.sync.FileBasedFiltersDataSyncAction
import java.io.FileNotFoundException
import java.io.InputStream

internal class GoogleDriveFiltersDataSyncAction(
        context: Context,
        val drive: Drive
) : FileBasedFiltersDataSyncAction<CloseableAny<File>, GoogleDriveUploadSession<FiltersData>>(context) {

    private val fileName = "filters.xml"

    private lateinit var commonFolderId: String
    private val files = drive.files()

    override fun newLoadFromRemoteSession(): CloseableAny<File> {
        val file = files.getOrNull(fileName, xmlMimeType, commonFolderId) ?: throw FileNotFoundException()
        return CloseableAny(file)
    }

    override fun CloseableAny<File>.getRemoteLastModified(): Long {
        return (obj.modifiedTime ?: obj.createdTime)?.value ?: 0
    }

    override fun CloseableAny<File>.loadFromRemote(): FiltersData {
        val data = FiltersData()
        data.parse(files.get(obj.id).executeAsInputStream().newPullParser(charset = Charsets.UTF_8))
        data.initFields()
        return data
    }

    override fun GoogleDriveUploadSession<FiltersData>.setRemoteLastModified(lastModified: Long) {
        this.localModifiedTime = lastModified
    }

    override fun GoogleDriveUploadSession<FiltersData>.saveToRemote(data: FiltersData): Boolean {
        return this.uploadData(data)
    }

    override fun newSaveToRemoteSession(): GoogleDriveUploadSession<FiltersData> {
        return object : GoogleDriveUploadSession<FiltersData>(fileName, commonFolderId, xmlMimeType, files) {
            override fun FiltersData.toInputStream(): InputStream {
                val os = DirectByteArrayOutputStream()
                this.serialize(os.newSerializer(charset = Charsets.UTF_8, indent = true))
                return os.inputStream(true)
            }
        }
    }


    override fun setup(): Boolean {
        commonFolderId = files.getOrCreate("Common", folderMimeType).id
        return true
    }

}