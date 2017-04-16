/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.provider

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import okio.ByteString
import org.mariotaku.mediaviewer.library.FileCache
import de.vanita5.twittnuker.TwittnukerConstants.AUTHORITY_TWITTNUKER_CACHE
import de.vanita5.twittnuker.TwittnukerConstants.QUERY_PARAM_TYPE
import de.vanita5.twittnuker.annotation.CacheFileType
import de.vanita5.twittnuker.model.CacheMetadata
import de.vanita5.twittnuker.task.SaveFileTask
import de.vanita5.twittnuker.util.BitmapUtils
import de.vanita5.twittnuker.util.JsonSerializer
import de.vanita5.twittnuker.util.dagger.GeneralComponent
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class CacheProvider : ContentProvider() {
    @Inject
    internal lateinit var fileCache: FileCache

    override fun onCreate(): Boolean {
        GeneralComponent.get(context).inject(this)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        val metadata = getMetadata(uri)
        if (metadata != null) {
            return metadata.contentType
        }
        val type = uri.getQueryParameter(QUERY_PARAM_TYPE)
        when (type) {
            CacheFileType.IMAGE -> {
                val file = fileCache.get(getCacheKey(uri)) ?: return null
                return BitmapUtils.getImageMimeType(file)
            }
            CacheFileType.VIDEO -> {
                return "video/mp4"
            }
            CacheFileType.JSON -> {
                return "application/json"
            }
        }
        return null
    }

    fun getMetadata(uri: Uri): CacheMetadata? {
        val bytes = fileCache.getExtra(getCacheKey(uri)) ?: return null
        return try {
            ByteArrayInputStream(bytes).use {
                return@use JsonSerializer.parse(it, CacheMetadata::class.java)
            }
        } catch (e: IOException) {
            null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        try {
            val file = fileCache.get(getCacheKey(uri)) ?: throw FileNotFoundException()
            val modeBits = modeToMode(mode)
            if (modeBits != ParcelFileDescriptor.MODE_READ_ONLY)
                throw IllegalArgumentException("Cache can't be opened for write")
            return ParcelFileDescriptor.open(file, modeBits)
        } catch (e: IOException) {
            throw FileNotFoundException()
        }

    }

    class ContentUriFileInfo(
            private val context: Context,
            private val uri: Uri,
            @CacheFileType override val cacheFileType: String?
    ) : SaveFileTask.FileInfo, CacheFileTypeSupport {
        override val fileName: String by lazy {
            var cacheKey = getCacheKey(uri)
            val indexOfSsp = cacheKey.indexOf("://")
            if (indexOfSsp != -1) {
                cacheKey = cacheKey.substring(indexOfSsp + 3)
            }
            return@lazy cacheKey.replace("[^\\w\\d_]".toRegex(), specialCharacter.toString())
        }

        override val mimeType: String? by lazy {
            if (cacheFileType == null || uri.getQueryParameter(QUERY_PARAM_TYPE) != null) {
                return@lazy context.contentResolver.getType(uri)
            }
            val builder = uri.buildUpon()
            builder.appendQueryParameter(QUERY_PARAM_TYPE, cacheFileType)
            return@lazy context.contentResolver.getType(builder.build())
        }

        override val specialCharacter: Char
            get() = '_'

        override fun inputStream(): InputStream {
            return context.contentResolver.openInputStream(uri)
        }

        override fun close() {
            // No-op
        }
    }

    interface CacheFileTypeSupport {
        val cacheFileType: String?
    }


    companion object {

        fun getCacheUri(key: String, @CacheFileType type: String?): Uri {
            val builder = Uri.Builder()
            builder.scheme(ContentResolver.SCHEME_CONTENT)
            builder.authority(AUTHORITY_TWITTNUKER_CACHE)
            builder.appendPath(ByteString.encodeUtf8(key).base64Url())
            if (type != null) {
                builder.appendQueryParameter(QUERY_PARAM_TYPE, type)
            }
            return builder.build()
        }

        fun getCacheKey(uri: Uri): String {
            if (ContentResolver.SCHEME_CONTENT != uri.scheme)
                throw IllegalArgumentException(uri.toString())
            if (AUTHORITY_TWITTNUKER_CACHE != uri.authority)
                throw IllegalArgumentException(uri.toString())
            return ByteString.decodeBase64(uri.lastPathSegment).utf8()
        }


        /**
         * Copied from ContentResolver.java
         */
        private fun modeToMode(mode: String): Int {
            val modeBits: Int
            if ("r" == mode) {
                modeBits = ParcelFileDescriptor.MODE_READ_ONLY
            } else if ("w" == mode || "wt" == mode) {
                modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            } else if ("wa" == mode) {
                modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_APPEND
            } else if ("rw" == mode) {
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE
            } else if ("rwt" == mode) {
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE or ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_TRUNCATE
            } else {
                throw IllegalArgumentException("Invalid mode: " + mode)
            }
            return modeBits
        }
    }
}