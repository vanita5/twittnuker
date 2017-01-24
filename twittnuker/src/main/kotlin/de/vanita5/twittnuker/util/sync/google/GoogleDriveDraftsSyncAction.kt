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
import com.google.api.services.drive.model.File
import de.vanita5.twittnuker.extension.model.filename
import de.vanita5.twittnuker.extension.model.readMimeMessageFrom
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.util.sync.FileBasedDraftsSyncAction
import java.io.IOException
import java.util.*

class GoogleDriveDraftsSyncAction(
        context: Context,
        val drive: Drive
) : FileBasedDraftsSyncAction<GoogleDriveDraftsSyncAction.DriveFileInfo>(context) {
    @Throws(IOException::class)
    override fun Draft.saveToRemote(): DriveFileInfo {
        try {
            val filename = "/Drafts/$filename"
            val modifiedTime = DateTime(timestamp)
            val create = drive.files().create(File().setName(filename).setModifiedTime(modifiedTime))
            val file = create.execute()
            return DriveFileInfo(file.id, file.originalFilename, Date())
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun Draft.loadFromRemote(info: DriveFileInfo): Boolean {
        try {
            val get = drive.files().get(info.fileId)
            get.executeAsInputStream().use {
                val parsed = this.readMimeMessageFrom(context, it)
                if (parsed) {
                    this.timestamp = info.draftTimestamp
                    this.unique_id = info.draftFileName.substringBeforeLast(".eml")
                }
                return parsed
            }
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun removeDrafts(list: List<DriveFileInfo>): Boolean {
        try {
            list.forEach { info ->
                drive.files().delete(info.fileId).execute()
            }
            return true
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun removeDraft(info: DriveFileInfo): Boolean {
        try {
            drive.files().delete(info.fileId).execute()
            return true
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    override val DriveFileInfo.draftTimestamp: Long get() = this.modifiedDate.time

    override val DriveFileInfo.draftFileName: String get() = this.name

    @Throws(IOException::class)
    override fun listRemoteDrafts(): List<DriveFileInfo> {
        val result = ArrayList<DriveFileInfo>()
        try {
            val list = drive.files().list()
            list.execute().files.mapTo(result) { file ->
                DriveFileInfo(file.id, file.originalFilename, Date(file.modifiedTime.value))
            }
        } catch (e: Exception) {
            throw IOException(e)
        }
        return result
    }

    data class DriveFileInfo(val fileId: String, val name: String, val modifiedDate: Date)

}