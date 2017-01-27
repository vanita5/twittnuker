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

package de.vanita5.twittnuker.util.sync.google

import android.content.Context
import com.google.api.client.util.DateTime
import com.google.api.services.drive.Drive
import de.vanita5.twittnuker.extension.model.filename
import de.vanita5.twittnuker.extension.model.readMimeMessageFrom
import de.vanita5.twittnuker.extension.model.writeMimeMessageTo
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.util.io.DirectByteArrayOutputStream
import de.vanita5.twittnuker.util.sync.FileBasedDraftsSyncAction
import java.io.IOException
import java.util.*


internal class GoogleDriveDraftsSyncAction(
        context: Context,
        val drive: Drive
) : FileBasedDraftsSyncAction<DriveFileInfo>(context) {

    val draftsDirName = "Drafts"
    val draftMimeType = "message/rfc822"

    private lateinit var folderId: String
    private val files = drive.files()

    @Throws(IOException::class)
    override fun Draft.saveToRemote(): DriveFileInfo {
        val os = DirectByteArrayOutputStream()
        this.writeMimeMessageTo(context, os)
        val file = files.updateOrCreate(filename, draftMimeType, folderId, stream = os.inputStream(true), fileConfig = {
            it.modifiedTime = DateTime(timestamp)
        })
        return DriveFileInfo(file.id, file.name, Date(timestamp))
    }

    @Throws(IOException::class)
    override fun Draft.loadFromRemote(info: DriveFileInfo): Boolean {
        val get = files.get(info.fileId)
        get.executeAsInputStream().use {
            val parsed = this.readMimeMessageFrom(context, it)
            if (parsed) {
                this.timestamp = info.draftTimestamp
                this.unique_id = info.draftFileName.substringBeforeLast(".eml")
            }
            return parsed
        }
    }

    @Throws(IOException::class)
    override fun removeDrafts(list: List<DriveFileInfo>): Boolean {
        list.forEach { info ->
            files.delete(info.fileId).execute()
        }
        return true
    }

    @Throws(IOException::class)
    override fun removeDraft(info: DriveFileInfo): Boolean {
        files.delete(info.fileId).execute()
        return true
    }

    override val DriveFileInfo.draftTimestamp: Long get() = this.modifiedDate.time

    override val DriveFileInfo.draftFileName: String get() = this.name

    @Throws(IOException::class)
    override fun listRemoteDrafts(): List<DriveFileInfo> {
        val result = ArrayList<DriveFileInfo>()
        var pageToken: String?
        do {
            val executeResult = files.list().apply {
                q = "'$folderId' in parents and mimeType = '$draftMimeType' and trashed = false"
            }.execute()
            executeResult.files.filter { file ->
                file.mimeType == draftMimeType
            }.mapTo(result) { file ->
                val lastModified = file.modifiedTime ?: file.createdTime
                DriveFileInfo(file.id, file.name, Date(lastModified?.value ?: 0))
            }
            pageToken = executeResult.nextPageToken
        } while (pageToken != null)
        return result
    }

    override fun setup(): Boolean {
        folderId = files.getOrCreate(draftsDirName, folderMimeType).id
        return true
    }

}