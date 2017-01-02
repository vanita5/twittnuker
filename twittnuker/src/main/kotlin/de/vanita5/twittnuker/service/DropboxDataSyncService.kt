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

package de.vanita5.twittnuker.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.support.v7.app.NotificationCompat
import android.util.Xml
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.map
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.dropboxAuthTokenKey
import de.vanita5.twittnuker.extension.model.*
import de.vanita5.twittnuker.model.DraftCursorIndices
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.provider.TwidereDataStore.Drafts
import java.io.*

class DropboxDataSyncService : BaseIntentService("dropbox_data_sync") {
    private val NOTIFICATION_ID_SYNC_DATA = 302

    override fun onHandleIntent(intent: Intent?) {
        val authToken = preferences[dropboxAuthTokenKey] ?: return
        val nb = NotificationCompat.Builder(this)
        nb.setSmallIcon(R.drawable.ic_stat_refresh)
        nb.setOngoing(true)
        nb.setContentTitle("Syncing data")
        nb.setContentText("Syncing using Dropbox")
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID_SYNC_DATA, nb.build())
        val requestConfig = DbxRequestConfig.newBuilder("twittnuker-android/${BuildConfig.VERSION_NAME}")
                .build()
        val client = DbxClientV2(requestConfig, authToken)
        syncFilters(client)
        uploadDrafts(client)
        nm.cancel(NOTIFICATION_ID_SYNC_DATA)
    }

    private fun uploadDrafts(client: DbxClientV2) {
        val cur = contentResolver.query(Drafts.CONTENT_URI, Drafts.COLUMNS, null, null, null) ?: return
        cur.map(DraftCursorIndices(cur)).forEach { draft ->
            client.newUploader("/Drafts/${draft.timestamp}.eml").use {
                draft.writeMimeMessageTo(this, it.outputStream)
                it.finish()
            }
        }
        cur.close()
    }

    @Throws(IOException::class)
    private fun syncFilters(client: DbxClientV2) {
        val helper = DropboxFiltersDataSyncHelper(this, client)
        helper.sync()
    }

    abstract class FileBasedFiltersDataSyncHelper(val context: Context) {
        @Throws(IOException::class)
        protected abstract fun loadFromRemote(): FiltersData

        @Throws(IOException::class)
        protected abstract fun saveToRemote(data: FiltersData)

        fun sync() {
            val remoteFilters: FiltersData = loadFromRemote()

            val filters: FiltersData = FiltersData().apply {
                read(context.contentResolver)
                initFields()
            }

            val syncDataDir: File = context.syncDataDir.apply {
                if (!exists()) {
                    mkdirs()
                }
            }
            val snapshotFile = File(syncDataDir, "filters.xml")
            val deletedFilters: FiltersData? = try {
                FileReader(snapshotFile).use {
                    val result = FiltersData()
                    val parser = Xml.newPullParser()
                    parser.setInput(it)
                    result.parse(parser)
                    result.removeAll(filters)
                    return@use result
                }
            } catch (e: FileNotFoundException) {
                null
            }

            filters.addAll(remoteFilters, true)

            if (deletedFilters != null) {
                filters.removeAll(deletedFilters)
            }

            filters.write(context.contentResolver)

            saveToRemote(filters)
            try {
                FileWriter(snapshotFile).use {
                    val serializer = Xml.newSerializer()
                    serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                    serializer.setOutput(it)
                    filters.serialize(serializer)
                }
            } catch (e: FileNotFoundException) {
                // Ignore
            }
        }

        private val Context.syncDataDir: File
            get() = File(filesDir, "sync_data")
    }

    class DropboxFiltersDataSyncHelper(context: Context, val client: DbxClientV2) : FileBasedFiltersDataSyncHelper(context) {
        override fun loadFromRemote(): FiltersData = client.newDownloader("/Common/filters.xml").use { downloader ->
            val result = FiltersData()
            val parser = Xml.newPullParser()
            parser.setInput(downloader.inputStream, "UTF-8")
            result.parse(parser)
            result.initFields()
            return@use result
        }

        override fun saveToRemote(data: FiltersData) {
            client.newUploader("/Common/filters.xml").use { uploader ->
                val serializer = Xml.newSerializer()
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                serializer.setOutput(uploader.outputStream, "UTF-8")
                data.serialize(serializer)
                uploader.finish()
            }
        }

    }

}


private fun DbxClientV2.newUploader(path: String) = files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).withMute(true).start()
private fun DbxClientV2.newDownloader(path: String) = files().downloadBuilder(path).start()