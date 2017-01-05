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

package de.vanita5.twittnuker.model.analyzer

import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.util.Analyzer


data class UpdateStatus(
        @AccountType override val accountType: String? = null,
        @Draft.Action val actionType: String?,
        @ParcelableMedia.Type val mediaType: Int,
        val hasLocation: Boolean,
        val preciseLocation: Boolean,
        val success: Boolean
) : Analyzer.Event {

    private val locationType: String get() = if (!hasLocation) {
        "none"
    } else if (preciseLocation) {
        "coordinate"
    } else {
        "place"
    }

    override val name: String
        get() = "Tweet"

    override fun forEachValues(action: (String, String?) -> Unit) {
        action("Status Type", actionTypeString(actionType))
        action("Media Type", mediaTypeString(mediaType))
        action("Location Type", locationType)
        action("Success", success.toString())
    }

    fun actionTypeString(@Draft.Action action: String?): String {
        return when (action) {
            Draft.Action.QUOTE -> "quote"
            Draft.Action.REPLY -> "reply"
            else -> "tweet"
        }
    }

    fun mediaTypeString(@ParcelableMedia.Type type: Int): String {
        return when (type) {
            ParcelableMedia.Type.IMAGE -> "image"
            ParcelableMedia.Type.VIDEO -> "video"
            ParcelableMedia.Type.ANIMATED_GIF -> "gif"
            ParcelableMedia.Type.CARD_ANIMATED_GIF -> "gif"
            ParcelableMedia.Type.EXTERNAL_PLAYER -> "external"
            ParcelableMedia.Type.VARIABLE_TYPE -> "variable"
            else -> "unknown"
        }
    }
}