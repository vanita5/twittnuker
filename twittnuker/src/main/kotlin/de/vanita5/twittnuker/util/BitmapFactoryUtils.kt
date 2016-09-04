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

package de.vanita5.twittnuker.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import java.io.IOException
import java.io.InputStream

object BitmapFactoryUtils {

    @Throws(IOException::class)
    fun decodeUri(contentResolver: ContentResolver, uri: Uri, outPadding: Rect?,
                  opts: BitmapFactory.Options?, close: Boolean = true): Bitmap? {
        var st: InputStream? = null
        try {
            st = contentResolver.openInputStream(uri)
            return BitmapFactory.decodeStream(st, outPadding, opts)
        } finally {
            if (close) {
                Utils.closeSilently(st)
            }
        }
    }

}