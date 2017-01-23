/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import com.dropbox.core.DbxDownloader
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DownloadErrorException
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.UploadUploader
import com.dropbox.core.v2.files.WriteMode
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


@Throws(IOException::class)
internal fun DbxClientV2.newUploader(path: String, clientModified: Long): UploadUploader {
    try {
        return files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).withMute(true)
                .withClientModified(Date(clientModified)).start()
    } catch (e: DbxException) {
        throw IOException(e)
    }
}

@Throws(IOException::class)
internal fun DbxClientV2.newDownloader(path: String): DbxDownloader<FileMetadata> {
    try {
        return files().downloadBuilder(path).start()
    } catch (e: DownloadErrorException) {
        if (e.errorValue?.pathValue?.isNotFound ?: false) {
            throw FileNotFoundException(path)
        }
        throw IOException(e)
    } catch (e: DbxException) {
        throw IOException(e)
    }
}