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

package de.vanita5.twittnuker.util.sync.google

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import java.io.InputStream


internal const val folderMimeType = "application/vnd.google-apps.folder"
internal const val xmlMimeType = "application/xml"


internal fun Drive.Files.getOrNull(name: String, mimeType: String?, parent: String? = "root",
                                   trashed: Boolean = false): File? {
    val find = list()
    var query = "name = '$name'"
    if (parent != null) {
        query += " and '$parent' in parents"
    }
    if (mimeType != null) {
        query += " and mimeType = '$mimeType'"
    }
    query += " and trashed = $trashed"
    find.q = query
    try {
        return find.execute().files.firstOrNull()
    } catch (e: GoogleJsonResponseException) {
        if (e.statusCode == 404) {
            return null
        } else {
            throw e
        }
    }
}

internal fun Drive.Files.getOrCreate(name: String, mimeType: String, parent: String = "root",
                                     trashed: Boolean = false): File {
    return getOrNull(name, mimeType, parent, trashed) ?: run {
        val fileMetadata = File()
        fileMetadata.name = name
        fileMetadata.mimeType = mimeType
        fileMetadata.parents = listOf(parent)
        return@run create(fileMetadata).execute()
    }
}

internal fun Drive.Files.updateOrCreate(
        name: String,
        mimeType: String,
        parent: String = "root",
        trashed: Boolean = false,
        stream: InputStream,
        fileConfig: ((file: File) -> Unit)? = null
): File {
    return run {
        val find = list()
        find.q = "name = '$name' and '$parent' in parents and mimeType = '$mimeType' and trashed = $trashed"
        try {
            val file = find.execute().files.firstOrNull() ?: return@run null
            fileConfig?.invoke(file)
            return@run update(file.id, file, InputStreamContent(mimeType, stream)).execute()
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 404) {
                return@run null
            } else {
                throw e
            }
        }
    } ?: run {
        val file = File()
        file.name = name
        file.mimeType = mimeType
        file.parents = listOf(parent)
        fileConfig?.invoke(file)
        return@run create(file, InputStreamContent(mimeType, stream)).execute()
    }
}