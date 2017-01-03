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

package de.vanita5.twittnuker.util.sync

import android.content.Context
import android.util.Log
import android.util.Xml
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.extension.model.*
import de.vanita5.twittnuker.model.FiltersData
import java.io.*

abstract class FileBasedFiltersDataSyncHelper(val context: Context) {
    fun performSync(): Boolean {
        val syncDataDir: File = context.syncDataDir.mkdirIfNotExists() ?: return false
        val snapshotFile = File(syncDataDir, "filters.xml")

        val remoteFilters = FiltersData()
        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG_SYNC, "Downloading remote filters")
        }
        val remoteModified = remoteFilters.loadFromRemote(snapshotFile.lastModified())
        if (BuildConfig.DEBUG && !remoteModified) {
            Log.d(LOGTAG_SYNC, "Remote filter unchanged, skip download")
        }
        val filters: FiltersData = FiltersData().apply {
            read(context.contentResolver)
            initFields()
        }

        var localModified = false

        val remoteAddedFilters = FiltersData()
        val remoteDeletedFilters = FiltersData()
        try {
            val snapshot = FileReader(snapshotFile).use {
                val snapshot = FiltersData()
                val parser = Xml.newPullParser()
                parser.setInput(it)
                snapshot.parse(parser)
                return@use snapshot
            }
            if (remoteModified) {
                remoteAddedFilters.addAll(remoteFilters)
                remoteAddedFilters.removeAll(snapshot)

                remoteDeletedFilters.addAll(snapshot)
                remoteDeletedFilters.removeAll(remoteFilters)
            }

            localModified = localModified or (snapshot != filters)
        } catch (e: FileNotFoundException) {
            remoteAddedFilters.addAll(remoteFilters)
            remoteAddedFilters.removeAll(filters)

            localModified = true
        }

        if (remoteModified) {
            filters.addAll(remoteAddedFilters, true)
            filters.removeAll(remoteDeletedFilters)

            localModified = !remoteAddedFilters.isEmpty() || !remoteDeletedFilters.isEmpty()
        }

        filters.write(context.contentResolver)

        val localModifiedTime = System.currentTimeMillis()

        if (localModified) {
            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG_SYNC, "Uploading filters")
            }
            filters.saveToRemote(localModifiedTime)
        } else if (BuildConfig.DEBUG) {
            Log.d(LOGTAG_SYNC, "Local not modified, skip upload")
        }
        try {
            FileWriter(snapshotFile).use {
                val serializer = Xml.newSerializer()
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                serializer.setOutput(it)
                filters.serialize(serializer)
            }
            snapshotFile.setLastModified(localModifiedTime)
        } catch (e: FileNotFoundException) {
            // Ignore
        }

        if (BuildConfig.DEBUG) {
            Log.d(LOGTAG_SYNC, "Filters sync complete")
        }
        return true
    }

    /**
     * Return false if remote not changed
     */
    @Throws(IOException::class)
    protected abstract fun FiltersData.loadFromRemote(snapshotModifiedMillis: Long): Boolean

    @Throws(IOException::class)
    protected abstract fun FiltersData.saveToRemote(localModifiedTime: Long): Boolean

}