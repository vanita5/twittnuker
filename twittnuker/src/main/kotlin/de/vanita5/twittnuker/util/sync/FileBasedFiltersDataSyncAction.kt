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
import android.util.Xml
import org.mariotaku.ktextension.nullableContentEquals
import de.vanita5.twittnuker.extension.model.*
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.provider.TwidereDataStore.Filters
import de.vanita5.twittnuker.util.content.ContentResolverUtils
import java.io.Closeable
import java.io.File
import java.io.IOException

abstract class FileBasedFiltersDataSyncAction<DownloadSession : Closeable, UploadSession : Closeable>(
        val context: Context
) : SingleFileBasedDataSyncAction<FiltersData, File, DownloadSession, UploadSession>() {

    override fun File.loadSnapshot(): FiltersData {
        return reader().use {
            val snapshot = FiltersData()
            val parser = Xml.newPullParser()
            parser.setInput(it)
            snapshot.parse(parser)
            return@use snapshot
        }
    }

    override fun File.saveSnapshot(data: FiltersData) {
        writer().use {
            val serializer = Xml.newSerializer()
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
            serializer.setOutput(it)
            data.serialize(serializer)
        }
    }

    override var File.snapshotLastModified: Long
        get() = this.lastModified()
        set(value) {
            this.setLastModified(value)
        }

    override fun loadFromLocal(): FiltersData {
        return FiltersData().apply {
            read(context.contentResolver)
            initFields()
        }
    }

    override fun addToLocal(data: FiltersData) {
        data.write(context.contentResolver, deleteOld = false)
    }

    override fun removeFromLocal(data: FiltersData) {
        ContentResolverUtils.bulkDelete(context.contentResolver, Filters.Users.CONTENT_URI,
                Filters.Users.USER_KEY, false, data.users?.map { it.userKey }, null, null)
        ContentResolverUtils.bulkDelete(context.contentResolver, Filters.Keywords.CONTENT_URI,
                Filters.Keywords.VALUE, false, data.keywords?.map { it.value }, null, null)
        ContentResolverUtils.bulkDelete(context.contentResolver, Filters.Sources.CONTENT_URI,
                Filters.Sources.VALUE, false, data.sources?.map { it.value }, null, null)
        ContentResolverUtils.bulkDelete(context.contentResolver, Filters.Links.CONTENT_URI,
                Filters.Links.VALUE, false, data.links?.map { it.value }, null, null)
    }

    override fun newData(): FiltersData {
        return FiltersData()
    }

    override fun FiltersData.minus(data: FiltersData): FiltersData {
        val diff = FiltersData()
        diff.addAll(this, true)
        diff.removeAllData(data)
        return diff
    }

    override fun FiltersData.addAllData(data: FiltersData): Boolean {
        return this.addAll(data, ignoreDuplicates = true)
    }

    override fun FiltersData.removeAllData(data: FiltersData): Boolean {
        return this.removeAll(data)
    }

    override fun FiltersData.isDataEmpty(): Boolean {
        return this.isEmpty()
    }

    override fun newSnapshotStore(): File {
        val syncDataDir: File = context.syncDataDir.mkdirIfNotExists() ?: throw IOException()
        return File(syncDataDir, "filters.xml")
    }

    override fun FiltersData.dataContentEquals(localData: FiltersData): Boolean {
        return this.users.nullableContentEquals(localData.users)
                && this.keywords.nullableContentEquals(localData.keywords)
                && this.sources.nullableContentEquals(localData.sources)
                && this.links.nullableContentEquals(localData.links)
    }

    override val whatData: String = "filters"
}