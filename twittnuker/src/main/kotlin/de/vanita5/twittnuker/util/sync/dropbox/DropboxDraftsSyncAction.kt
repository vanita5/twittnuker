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

import android.content.Context
import com.dropbox.core.DbxException
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DeleteArg
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderErrorException
import com.dropbox.core.v2.files.ListFolderResult
import de.vanita5.twittnuker.extension.model.filename
import de.vanita5.twittnuker.extension.model.readMimeMessageFrom
import de.vanita5.twittnuker.extension.model.writeMimeMessageTo
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.util.sync.FileBasedDraftsSyncAction
import java.io.IOException
import java.util.*

class DropboxDraftsSyncAction(context: Context, val client: DbxClientV2) : FileBasedDraftsSyncAction<FileMetadata>(context) {
    @Throws(IOException::class)
    override fun Draft.saveToRemote(): FileMetadata {
        try {
            client.newUploader("/Drafts/$filename", this.timestamp).use {
                this.writeMimeMessageTo(context, it.outputStream)
                return it.finish()
            }
        } catch (e: DbxException) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun Draft.loadFromRemote(info: FileMetadata): Boolean {
        try {
            client.files().download(info.pathLower).use {
                val parsed = this.readMimeMessageFrom(context, it.inputStream)
                if (parsed) {
                    this.timestamp = info.draftTimestamp
                    this.unique_id = info.draftFileName.substringBeforeLast(".eml")
                }
                return parsed
            }
        } catch (e: DbxException) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun removeDrafts(list: List<FileMetadata>): Boolean {
        try {
            return client.files().deleteBatch(list.map { DeleteArg(it.pathLower) }) != null
        } catch (e: DbxException) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun removeDraft(info: FileMetadata): Boolean {
        try {
            return client.files().delete(info.pathLower) != null
        } catch (e: DbxException) {
            throw IOException(e)
        }
    }

    override val FileMetadata.draftTimestamp: Long get() = this.clientModified.time

    override val FileMetadata.draftFileName: String get() = this.name

    @Throws(IOException::class)
    override fun listRemoteDrafts(): List<FileMetadata> {
        val result = ArrayList<FileMetadata>()
        try {
            var listResult: ListFolderResult = client.files().listFolder("/Drafts/")
            while (true) {
                // Do something with files
                listResult.entries.mapNotNullTo(result) { it as? FileMetadata }
                if (!listResult.hasMore) break
                listResult = client.files().listFolderContinue(listResult.cursor)
            }
        } catch (e: DbxException) {
            if (e is ListFolderErrorException) {
                if (e.errorValue?.pathValue?.isNotFound ?: false) {
                    return emptyList()
                }
            }
            throw IOException(e)
        }
        return result
    }

}