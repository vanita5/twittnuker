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
import com.dropbox.core.DbxDownloader
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadUploader
import de.vanita5.twittnuker.extension.model.initFields
import de.vanita5.twittnuker.extension.model.parse
import de.vanita5.twittnuker.extension.model.serialize
import de.vanita5.twittnuker.extension.newPullParser
import de.vanita5.twittnuker.extension.newSerializer
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.util.sync.FileBasedFiltersDataSyncAction

internal class DropboxFiltersDataSyncAction(
        context: Context,
        val client: DbxClientV2
) : FileBasedFiltersDataSyncAction<DbxDownloader<FileMetadata>, DropboxUploadSession<FiltersData>>(context) {
    override fun DbxDownloader<FileMetadata>.getRemoteLastModified(): Long {
        return result.clientModified.time
    }

    private val filePath = "/Common/filters.xml"

    override fun newLoadFromRemoteSession(): DbxDownloader<FileMetadata> {
        return client.newDownloader(filePath)
    }

    override fun DbxDownloader<FileMetadata>.loadFromRemote(): FiltersData {
        val data = FiltersData()
        data.parse(inputStream.newPullParser(charset = Charsets.UTF_8))
        data.initFields()
        return data
    }

    override fun DropboxUploadSession<FiltersData>.setRemoteLastModified(lastModified: Long) {
        this.localModifiedTime = lastModified
    }

    override fun DropboxUploadSession<FiltersData>.saveToRemote(data: FiltersData): Boolean {
        return this.uploadData(data)
    }

    override fun newSaveToRemoteSession(): DropboxUploadSession<FiltersData> {
        return object : DropboxUploadSession<FiltersData>(filePath, client) {
            override fun performUpload(uploader: UploadUploader, data: FiltersData) {
                data.serialize(uploader.outputStream.newSerializer(charset = Charsets.UTF_8, indent = true))
            }
        }
    }

}