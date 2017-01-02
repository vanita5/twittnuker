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

import android.content.Intent
import android.util.Xml
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.map
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.dropboxAuthTokenKey
import de.vanita5.twittnuker.extension.model.read
import de.vanita5.twittnuker.extension.model.serialize
import de.vanita5.twittnuker.extension.model.writeMimeMessageTo
import de.vanita5.twittnuker.model.DraftCursorIndices
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.provider.TwidereDataStore.Drafts

class DropboxDataSyncService : BaseIntentService("dropbox_data_sync") {

    override fun onHandleIntent(intent: Intent?) {
        val authToken = preferences[dropboxAuthTokenKey] ?: return
        val requestConfig = DbxRequestConfig.newBuilder("twittnuker-android/${BuildConfig.VERSION_NAME}")
                .build()
        val client = DbxClientV2(requestConfig, authToken)
        uploadFilters(client)
        uploadDrafts(client)
    }

    private fun DbxClientV2.newUploader(path: String) = files().uploadBuilder(path).withMode(WriteMode.OVERWRITE).withMute(true).start()

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

    private fun uploadFilters(client: DbxClientV2) {
        val uploader = client.newUploader("/Common/filters.xml")
        val filters = FiltersData()
        filters.read(contentResolver)
        val serializer = Xml.newSerializer()
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
        uploader.use {
            serializer.setOutput(it.outputStream, "UTF-8")
            filters.serialize(serializer)
            it.finish()
        }
    }

}
