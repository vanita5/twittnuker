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

package de.vanita5.twittnuker.util.sync

import android.content.Context
import android.support.v4.util.LongSparseArray
import org.mariotaku.ktextension.map
import org.mariotaku.ktextension.set
import org.mariotaku.library.objectcursor.ObjectCursor
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.extension.model.filename
import de.vanita5.twittnuker.extension.model.unique_id_non_null
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.provider.TwidereDataStore.Drafts
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

abstract class FileBasedDraftsSyncAction<RemoteFileInfo>(val context: Context) : ISyncAction {

    @Throws(IOException::class)
    override fun execute(): Boolean {
        DebugLog.d(LOGTAG_SYNC, "Begin syncing drafts")

        if (!setup()) {
            return false
        }

        val syncDataDir: File = context.syncDataDir.mkdirIfNotExists() ?: return false
        val snapshotsListFile = File(syncDataDir, "draft_ids.list")

        // Read last synced id
        val snapshotIds: List<String> = try {
            snapshotsListFile.readLines()
        } catch (e: FileNotFoundException) {
            emptyList<String>()
        }

        val localDrafts = run {
            val cur = context.contentResolver.query(Drafts.CONTENT_URI, Drafts.COLUMNS, null, null, null)!!
            try {
                val indices = ObjectCursor.indicesFrom(cur, Draft::class.java)
                return@run cur.map(indices)
            } finally {
                cur.close()
            }
        }

        // Ids of draft removed locally, we will delete these from remote storage
        val localRemovedIds = snapshotIds.filter { id -> localDrafts.none { draft -> draft.unique_id_non_null == id } }


        val remoteDrafts = listRemoteDrafts()
        // Download remote items
        val downloadRemoteInfoList = ArrayList<RemoteFileInfo>()
        // Remote remote items: snapshot has but database doesn't have
        val removeRemoteInfoList = ArrayList<RemoteFileInfo>()
        // Update local items using remote
        val updateLocalInfoList = LongSparseArray<RemoteFileInfo>()
        // Remove local items: snapshot has but remote doesn't have
        val removeLocalIdsList = snapshotIds.filter { snapshotId ->
            remoteDrafts.none { it.draftFileName == "$snapshotId.eml" }
        }

        val uploadLocalList = ArrayList<Draft>()

        remoteDrafts.forEach { remoteDraft ->
            val localDraft = localDrafts.find { it.filename == remoteDraft.draftFileName }
            if (remoteDraft.draftFileName.substringBefore(".eml") in localRemovedIds) {
                // Local removed, remove remote
                removeRemoteInfoList.add(remoteDraft)
            } else if (localDraft == null) {
                // Local doesn't exist, download remote
                downloadRemoteInfoList.add(remoteDraft)
            } else if (remoteDraft.draftTimestamp - localDraft.timestamp > 1000) {
                // Local is older, update from remote
                localDraft.remote_extras = remoteDraft.draftRemoteExtras
                updateLocalInfoList[localDraft._id] = remoteDraft
            } else if (localDraft.timestamp - remoteDraft.draftTimestamp > 1000) {
                // Local is newer, upload local
                localDraft.remote_extras = remoteDraft.draftRemoteExtras
                uploadLocalList.add(localDraft)
            }
        }

        // Deal with local drafts that remote doesn't have
        localDrafts.filterTo(uploadLocalList) { localDraft ->
            if (remoteDrafts.any { it.draftFileName == localDraft.filename }) {
                return@filterTo false
            }
            if (downloadRemoteInfoList.any { it.draftFileName == localDraft.filename }) {
                return@filterTo false
            }
            if (removeRemoteInfoList.any { it.draftFileName == localDraft.filename }) {
                return@filterTo false
            }
            if (localDraft.unique_id_non_null in removeLocalIdsList) {
                return@filterTo false
            }
            if ((0 until updateLocalInfoList.size()).any { updateLocalInfoList.valueAt(it).draftFileName == localDraft.filename }) {
                return@filterTo false
            }
            return@filterTo true
        }


        // Upload local items
        if (uploadLocalList.isNotEmpty()) {
            val fileList = uploadLocalList.joinToString(",") { it.filename }
            DebugLog.d(LOGTAG_SYNC, "Uploading local drafts $fileList")
            uploadDrafts(uploadLocalList)
        }

        // Download remote items
        if (downloadRemoteInfoList.isNotEmpty()) {
            val fileList = downloadRemoteInfoList.joinToString(",") { it.draftFileName }
            DebugLog.d(LOGTAG_SYNC, "Downloading remote drafts $fileList")
            ContentResolverUtils.bulkInsert(context.contentResolver, Drafts.CONTENT_URI,
                    downloadDrafts(downloadRemoteInfoList).map { ObjectCursor.valuesCreatorFrom(Draft::class.java).create(it) })
        }

        // Update local items
        if (updateLocalInfoList.size() > 0) {
            val fileList = (0 until updateLocalInfoList.size()).joinToString(",") { updateLocalInfoList.valueAt(it).draftFileName }
            DebugLog.d(LOGTAG_SYNC, "Updating local drafts $fileList")
            val creator = ObjectCursor.valuesCreatorFrom(Draft::class.java)
            for (index in 0 until updateLocalInfoList.size()) {
                val draft = Draft()
                if (draft.loadFromRemote(updateLocalInfoList.valueAt(index))) {
                    val where = Expression.equalsArgs(Drafts._ID).sql
                    val whereArgs = arrayOf(updateLocalInfoList.keyAt(index).toString())
                    context.contentResolver.update(Drafts.CONTENT_URI, creator.create(draft), where, whereArgs)
                }
            }
        }

        // Remove local items
        if (removeLocalIdsList.isNotEmpty()) {
            val fileList = removeLocalIdsList.joinToString(",") { "$it.eml" }
            DebugLog.d(LOGTAG_SYNC, "Removing local drafts $fileList")
            ContentResolverUtils.bulkDelete(context.contentResolver, Drafts.CONTENT_URI,
                    Drafts.UNIQUE_ID, false, removeLocalIdsList, null, null)
        }

        // Remove remote items
        if (removeRemoteInfoList.isNotEmpty()) {
            val fileList = removeRemoteInfoList.joinToString(",") { it.draftFileName }
            DebugLog.d(LOGTAG_SYNC, "Removing remote drafts $fileList")
            removeDrafts(removeRemoteInfoList)
        }

        snapshotsListFile.writer().use { writer ->
            val cur = context.contentResolver.query(Drafts.CONTENT_URI, Drafts.COLUMNS, null, null, null)!!
            try {
                val indices = ObjectCursor.indicesFrom(cur, Draft::class.java)
                cur.map(indices).map { it.unique_id_non_null }.forEach { line ->
                    writer.write(line)
                    writer.write("\n")
                }
            } finally {
                cur.close()
            }
        }

        DebugLog.d(LOGTAG_SYNC, "Finished syncing drafts")
        return true
    }

    @Throws(IOException::class)
    abstract fun listRemoteDrafts(): List<RemoteFileInfo>

    @Throws(IOException::class)
    open fun downloadDrafts(list: List<RemoteFileInfo>): List<Draft> {
        val result = ArrayList<Draft>()
        list.forEach {
            val draft = Draft()
            if (draft.loadFromRemote(it)) {
                result.add(draft)
            }
        }
        return result
    }

    @Throws(IOException::class)
    open fun removeDrafts(list: List<RemoteFileInfo>): Boolean {
        var result = false
        list.forEach { item ->
            result = result or removeDraft(item)
        }
        return result
    }

    @Throws(IOException::class)
    open fun uploadDrafts(list: List<Draft>): Boolean {
        var result = false
        list.forEach { item ->
            result = result or (item.saveToRemote() != null)
        }
        return result
    }

    @Throws(IOException::class)
    abstract fun Draft.loadFromRemote(info: RemoteFileInfo): Boolean

    @Throws(IOException::class)
    abstract fun removeDraft(info: RemoteFileInfo): Boolean

    @Throws(IOException::class)
    abstract fun Draft.saveToRemote(): RemoteFileInfo?

    abstract val RemoteFileInfo.draftFileName: String
    abstract val RemoteFileInfo.draftTimestamp: Long
    open val RemoteFileInfo.draftRemoteExtras: String? get() = null

    @Throws(IOException::class)
    open fun setup(): Boolean = true
}