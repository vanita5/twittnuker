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

import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.UploadUploader
import java.io.Closeable
import java.io.IOException

abstract internal class DropboxUploadSession<in Data>(val fileName: String, val client: DbxClientV2) : Closeable {
    private var uploader: UploadUploader? = null

    var localModifiedTime: Long = 0

    override fun close() {
        uploader?.close()
    }

    @Throws(IOException::class)
    abstract fun performUpload(uploader: UploadUploader, data: Data)

    fun uploadData(data: Data): Boolean {
        uploader = client.newUploader(fileName, localModifiedTime).apply {
            performUpload(this, data)
            this.finish()
        }
        return true
    }

}