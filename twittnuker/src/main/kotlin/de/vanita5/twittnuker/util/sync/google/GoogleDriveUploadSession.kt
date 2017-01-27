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

import com.dropbox.core.v2.files.UploadUploader
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import java.io.Closeable
import java.io.IOException
import java.io.InputStream

abstract internal class GoogleDriveUploadSession<in Data>(
        val name: String,
        val parentId: String,
        val mimeType: String,
        val drive: Drive
) : Closeable {
    private var uploader: UploadUploader? = null

    var localModifiedTime: Long = 0

    override fun close() {
        uploader?.close()
    }

    @Throws(IOException::class)
    abstract fun Data.toInputStream(): InputStream

    fun uploadData(data: Data): Boolean {
        drive.updateOrCreate(name = name, mimeType = mimeType, parent = parentId,
                spaces = appDataFolderSpace, stream = data.toInputStream(), fileConfig = {
            it.modifiedTime = DateTime(localModifiedTime)
        })
        return true
    }

}