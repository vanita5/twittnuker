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

package de.vanita5.twittnuker.extension.model

import android.annotation.SuppressLint
import de.vanita5.twittnuker.model.ParcelableMedia


fun parcelableMediaTypeString(@ParcelableMedia.Type type: Int): String? = when (type) {
        ParcelableMedia.Type.IMAGE -> "image"
        ParcelableMedia.Type.VIDEO -> "video"
        ParcelableMedia.Type.ANIMATED_GIF -> "gif"
        ParcelableMedia.Type.CARD_ANIMATED_GIF -> "gif"
        ParcelableMedia.Type.EXTERNAL_PLAYER -> "external"
        ParcelableMedia.Type.VARIABLE_TYPE -> "variable"
        ParcelableMedia.Type.UNKNOWN -> null
        else -> null
    }

@SuppressLint("SwitchIntDef")
fun ParcelableMedia.getBestVideoUrlAndType(supportedTypes: Array<String>): Pair<String, String?>? {
    val mediaUrl = media_url ?: return null
    when (type) {
        ParcelableMedia.Type.VIDEO, ParcelableMedia.Type.ANIMATED_GIF -> {
            val videoInfo = video_info ?: return Pair(mediaUrl, null)
            val firstMatch = videoInfo.variants.filter { variant ->
                supportedTypes.any { it.equals(variant.content_type, ignoreCase = true) }
            }.sortedByDescending(ParcelableMedia.VideoInfo.Variant::bitrate).firstOrNull() ?: return null
            return Pair(firstMatch.url, firstMatch.content_type)
        }
        ParcelableMedia.Type.CARD_ANIMATED_GIF -> {
            return Pair(mediaUrl, "video/mp4")
        }
        else -> {
            return null
        }
    }
}

val ParcelableMedia.aspect_ratio: Double
    get() {
        if (this.height <= 0 || this.width <= 0) return Double.NaN
        return this.width / this.height.toDouble()
    }