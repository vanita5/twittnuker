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

import com.google.api.client.googleapis.json.GoogleJsonError
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.HttpHeaders
import com.google.api.client.http.InputStreamContent
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.common.collect.HashMultimap
import java.io.IOException
import java.io.InputStream
import java.util.*


internal const val folderMimeType = "application/vnd.google-apps.folder"
internal const val xmlMimeType = "application/xml"
internal const val requiredRequestFields = "id, name, parents, mimeType, modifiedTime"
internal const val requiredFilesRequestFields = "files($requiredRequestFields)"

internal fun Drive.getFileOrNull(
        name: String,
        mimeType: String?,
        parent: String? = "root",
        trashed: Boolean = false,
        conflictResolver: ((Drive, List<File>) -> File)? = null
): File? {
    val result = findFilesOrNull(name, mimeType, parent, trashed) ?: return null
    if (result.size > 1 && conflictResolver != null) {
        return conflictResolver(this, result)
    }
    return result.firstOrNull()
}

internal fun Drive.findFilesOrNull(
        name: String,
        mimeType: String?,
        parent: String? = "root",
        trashed: Boolean = false
): List<File>? {
    val find = files().list()
    var query = "name = '$name'"
    if (parent != null) {
        query += " and '$parent' in parents"
    }
    if (mimeType != null) {
        query += " and mimeType = '$mimeType'"
    }
    query += " and trashed = $trashed"
    find.q = query
    find.fields = requiredFilesRequestFields
    try {
        val files = find.execute().files
        if (files.isEmpty()) return null
        return files
    } catch (e: GoogleJsonResponseException) {
        if (e.statusCode == 404) {
            return null
        } else {
            throw e
        }
    }
}

internal fun Drive.getFileOrCreate(
        name: String,
        mimeType: String,
        parent: String = "root",
        trashed: Boolean = false,
        conflictResolver: ((Drive, List<File>) -> File)? = null
): File {
    val result = findFilesOrCreate(name, mimeType, parent, trashed)
    if (result.size > 1 && conflictResolver != null) {
        return conflictResolver(this, result)
    }
    return result.first()
}

internal fun Drive.findFilesOrCreate(
        name: String,
        mimeType: String,
        parent: String = "root",
        trashed: Boolean = false
): List<File> {
    return findFilesOrNull(name, mimeType, parent, trashed) ?: run {
        val file = File()
        file.name = name
        file.mimeType = mimeType
        file.parents = listOf(parent)
        val create = files().create(file)
        return@run listOf(create.execute())
    }
}

internal fun Drive.updateOrCreate(
        name: String,
        mimeType: String,
        parent: String = "root",
        trashed: Boolean = false,
        stream: InputStream,
        fileConfig: ((file: File) -> Unit)? = null
): File {
    val files = files()
    return run {
        val find = files.list()
        find.q = "name = '$name' and '$parent' in parents and mimeType = '$mimeType' and trashed = $trashed"
        find.fields = requiredFilesRequestFields
        val fileId = try {
            find.execute().files.firstOrNull()?.id ?: return@run null
        } catch (e: GoogleJsonResponseException) {
            if (e.statusCode == 404) {
                return@run null
            } else {
                throw e
            }
        }
        return@run files.performUpdate(fileId, name, mimeType, stream, fileConfig)
    } ?: run {
        val file = File()
        file.name = name
        file.mimeType = mimeType
        file.parents = listOf(parent)
        fileConfig?.invoke(file)
        val create = files.create(file, InputStreamContent(mimeType, stream))
        create.fields = requiredRequestFields
        return@run create.execute()
    }
}

internal fun Drive.Files.performUpdate(
        fileId: String,
        name: String,
        mimeType: String,
        stream: InputStream,
        fileConfig: ((file: File) -> Unit)? = null
): File {
    val file = File()
    file.name = name
    file.mimeType = mimeType
    fileConfig?.invoke(file)
    val update = update(fileId, file, InputStreamContent(mimeType, stream))
    update.fields = requiredRequestFields
    return update.execute()
}

internal fun resolveFilesConflict(client: Drive, list: List<File>): File {
    // Pick newest file
    val newest = list.maxBy { it.modifiedTime.value }!!

    // Delete all others
    val batch = client.batch()
    val callback = SimpleJsonBatchCallback<Void>()
    val files = client.files()
    list.filterNot { it == newest }.forEach { files.delete(it.id).queue(batch, callback) }
    batch.execute()
    return newest
}

internal fun resolveFoldersConflict(client: Drive, list: List<File>): File {
    val files = client.files()

    // Pick newest folder
    val newest = list.maxBy { it.modifiedTime.value }!!

    // Build a map with all conflicting folders
    val query = list.joinToString(" or ") { "'${it.id}' in parents" }
    val filesList = ArrayList<File>()

    val conflictFilesMap = HashMultimap.create<String, File>()
    var nextPageToken: String? = null
    do {
        val result = files.list().apply {
            this.q = query
            this.fields = requiredFilesRequestFields
            if (nextPageToken != null) {
                this.pageToken = nextPageToken
            }
        }.execute()
        result.files.forEach { file ->
            file.parents.forEach { parentId ->
                if (parentId == newest.id) {
                    filesList.add(file)
                } else {
                    conflictFilesMap.put(parentId, file)
                }
            }
        }
        nextPageToken = result.nextPageToken
    } while (nextPageToken != null)

    // Files in this list will be moved to newest folder
    val insertList = ArrayList<File>()
    // Files in this list will be removed
    val removeList = ArrayList<File>()

    for ((k, l) in conflictFilesMap.asMap()) {
        for (v in l) {
            val find = filesList.find { it.name == v.name }
            if (find == null) {
                insertList.add(v)
            } else if (find.modifiedTime.value > v.modifiedTime.value) {
                // Our file is newer, remove `v`
                removeList.add(v)
            } else {
                // `v` is newer, update ours
                insertList.add(v)
                removeList.add(find)
            }
        }
    }

    list.filterNotTo(removeList) { it == newest }

    if (insertList.isNotEmpty()) {
        val callback = object : SimpleJsonBatchCallback<File>() {
            override fun onFailure(error: GoogleJsonError, headers: HttpHeaders) {
                throw IOException(error.message)
            }
        }
        client.batch().apply {
            insertList.forEach { file ->
                files.update(file.id, File()).apply {
                    this.addParents = newest.id
                    this.removeParents = file.parents?.joinToString(",")
                }.queue(this, callback)
            }
        }.execute()
    }

    if (removeList.isNotEmpty()) {
        val callback = SimpleJsonBatchCallback<Void>()
        client.batch().apply {
            removeList.forEach { file ->
                files.delete(file.id).queue(this, callback)
            }
        }.execute()
    }

    return newest

}