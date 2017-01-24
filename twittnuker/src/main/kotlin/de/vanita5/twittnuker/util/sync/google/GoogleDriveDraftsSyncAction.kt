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
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.MetadataChangeSet
import de.vanita5.twittnuker.extension.model.filename
import de.vanita5.twittnuker.extension.model.readMimeMessageFrom
import de.vanita5.twittnuker.extension.model.writeMimeMessageTo
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.util.sync.FileBasedDraftsSyncAction
import java.io.IOException
import java.util.*

class GoogleDriveDraftsSyncAction(
        context: Context,
        val client: GoogleApiClient
) : FileBasedDraftsSyncAction<GoogleDriveDraftsSyncAction.DriveFileInfo>(context) {
    @Throws(IOException::class)
    override fun Draft.saveToRemote(): DriveFileInfo {
        try {
            val folder = Drive.DriveApi.getAppFolder(client)
            val driveContents = Drive.DriveApi.newDriveContents(client).await().driveContents
            this.writeMimeMessageTo(context, driveContents.outputStream)
            val filename = "/Drafts/$filename"
            val changeSet = MetadataChangeSet.Builder().setTitle(filename).build()
            val driveFile = folder.createFile(client, changeSet, driveContents).await().driveFile
            return DriveFileInfo(driveFile.driveId, filename, Date())
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun Draft.loadFromRemote(info: DriveFileInfo): Boolean {
        try {
            val file = info.driveId.asDriveFile()
            val result = file.open(client, DriveFile.MODE_READ_ONLY, null).await()
            result.driveContents.inputStream.use {
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
                info.driveId.asDriveFile().delete(client).await()
            }
            return true
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    @Throws(IOException::class)
    override fun removeDraft(info: DriveFileInfo): Boolean {
        try {
            return info.driveId.asDriveFile().delete(client).await().isSuccess
        } catch (e: Exception) {
            throw IOException(e)
        }
    }

    override val DriveFileInfo.draftTimestamp: Long get() = this.modifiedDate.time

    override val DriveFileInfo.draftFileName: String get() = this.name

    @Throws(IOException::class)
    override fun listRemoteDrafts(): List<DriveFileInfo> {
        val pendingResult = Drive.DriveApi.getAppFolder(client).listChildren(client)
        val result = ArrayList<DriveFileInfo>()
        try {
            val requestResult = pendingResult.await()
            requestResult.metadataBuffer.mapTo(result) { metadata ->
                DriveFileInfo(metadata.driveId, metadata.originalFilename, metadata.modifiedDate)
            }
        } catch (e: Exception) {
            throw IOException(e)
        }
        return result
    }

    data class DriveFileInfo(val driveId: DriveId, val name: String, val modifiedDate: Date)

}