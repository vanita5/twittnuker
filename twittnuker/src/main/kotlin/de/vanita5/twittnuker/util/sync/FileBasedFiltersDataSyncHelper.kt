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
            Log.d(LOGTAG, "Downloading remote filters")
        }
        val remoteModified = remoteFilters.loadFromRemote(snapshotFile.lastModified())
        if (BuildConfig.DEBUG && !remoteModified) {
            Log.d(LOGTAG, "Remote filter unchanged, skipped downloading")
        }
        val filters: FiltersData = FiltersData().apply {
            read(context.contentResolver)
            initFields()
        }

        var localModified = false

        val deletedFilters: FiltersData? = try {
            FileReader(snapshotFile).use {
                val result = FiltersData()
                val parser = Xml.newPullParser()
                parser.setInput(it)
                result.parse(parser)
                localModified = localModified or (result != filters)
                result.removeAll(filters)
                return@use result
            }
        } catch (e: FileNotFoundException) {
            localModified = true
            null
        }

        if (remoteModified) {
            localModified = localModified or filters.addAll(remoteFilters, true)
        }

        if (deletedFilters != null) {
            localModified = localModified or filters.removeAll(deletedFilters)
        }

        filters.write(context.contentResolver)

        val localModifiedTime = System.currentTimeMillis()

        if (localModified) {
            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG, "Uploading filters")
            }
            filters.saveToRemote(localModifiedTime)
        } else if (BuildConfig.DEBUG) {
            Log.d(LOGTAG, "Local not modified, skip upload")
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
            Log.d(LOGTAG, "Filters sync complete")
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