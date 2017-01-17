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

package de.vanita5.twittnuker.util.sync.dropbox

import android.content.Context
import android.content.SharedPreferences
import com.dropbox.core.DbxDownloader
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.extension.model.*
import de.vanita5.twittnuker.extension.newPullParser
import de.vanita5.twittnuker.extension.newSerializer
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.util.TaskServiceRunner
import de.vanita5.twittnuker.util.sync.*
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*


class DropboxSyncTaskRunner(context: Context, val authToken: String) : SyncTaskRunner(context) {

    override fun onRunningTask(action: String, callback: (Boolean) -> Unit): Boolean {
        val requestConfig = DbxRequestConfig.newBuilder("twittnuker-android/${BuildConfig.VERSION_NAME}")
                .build()
        val client = DbxClientV2(requestConfig, authToken)
        val syncAction: ISyncAction = when (action) {
            TaskServiceRunner.ACTION_SYNC_DRAFTS -> DropboxDraftsSyncAction(context, client)
            TaskServiceRunner.ACTION_SYNC_FILTERS -> DropboxFiltersDataSyncAction(context, client)
            TaskServiceRunner.ACTION_SYNC_USER_COLORS -> DropboxPreferencesValuesSyncAction(context,
                    client, userColorNameManager.colorPreferences, UserColorsSyncProcessor,
                    "/Common/user_colors.xml")
            else -> null
        } ?: return false
        task {
            syncAction.execute()
        }.successUi {
            callback(true)
        }.failUi {
            callback(false)
        }
        return true
    }


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
                throw IOException(e)
            }
            return result
        }

    }

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


    internal class DropboxPreferencesValuesSyncAction(
            context: Context,
            val client: DbxClientV2,
            preferences: SharedPreferences,
            processor: Processor,
            val filePath: String
    ) : FileBasedPreferencesValuesSyncAction<DbxDownloader<FileMetadata>,
            DropboxUploadSession<Map<String, String>>>(context, preferences, processor) {
        override fun DbxDownloader<FileMetadata>.getRemoteLastModified(): Long {
            return result.clientModified.time
        }

        override fun DbxDownloader<FileMetadata>.loadFromRemote(): MutableMap<String, String> {
            val data = HashMap<String, String>()
            data.parse(inputStream.newPullParser())
            return data
        }

        override fun newLoadFromRemoteSession(): DbxDownloader<FileMetadata> {
            return client.newDownloader(filePath)
        }

        override fun newSaveToRemoteSession(): DropboxUploadSession<Map<String, String>> {
            return object : DropboxUploadSession<Map<String, String>>(filePath, client) {
                override fun performUpload(uploader: UploadUploader, data: Map<String, String>) {
                    data.serialize(uploader.outputStream.newSerializer(charset = Charsets.UTF_8,
                            indent = true))
                }
            }
        }

        override fun DropboxUploadSession<Map<String, String>>.saveToRemote(data: MutableMap<String, String>): Boolean {
            return this.uploadData(data)
        }

        override fun DropboxUploadSession<Map<String, String>>.setRemoteLastModified(lastModified: Long) {
            this.localModifiedTime = lastModified
        }
    }

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

    companion object {


        @Throws(IOException::class)
        private fun DbxClientV2.newUploader(path: String, clientModified: Long): UploadUploader {
            try {
                return files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).withMute(true)
                        .withClientModified(Date(clientModified)).start()
            } catch (e: DbxException) {
                throw IOException(e)
            }
        }

        @Throws(IOException::class)
        private fun DbxClientV2.newDownloader(path: String): DbxDownloader<FileMetadata> {
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
    }
}