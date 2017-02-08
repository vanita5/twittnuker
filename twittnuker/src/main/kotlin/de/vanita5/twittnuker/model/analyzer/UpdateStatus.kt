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

package de.vanita5.twittnuker.model.analyzer

import de.vanita5.twittnuker.library.MicroBlogException
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.extension.model.draftActionTypeString
import de.vanita5.twittnuker.extension.model.parcelableMediaTypeString
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.task.twitter.UpdateStatusTask
import de.vanita5.twittnuker.util.Analyzer
import java.io.IOException


data class UpdateStatus(
        @AccountType override val accountType: String? = null,
        @Draft.Action val actionType: String?,
        @ParcelableMedia.Type val mediaType: Int,
        val hasLocation: Boolean,
        val preciseLocation: Boolean,
        val success: Boolean,
        val exception: Exception?
) : Analyzer.Event {

    private val locationType: String get() = if (!hasLocation) {
        "none"
    } else if (preciseLocation) {
        "coordinate"
    } else {
        "place"
    }

    private val errorReason: String? get() {
        val ex = exception ?: return null
        when (ex) {
            is UpdateStatusTask.ShortenerNotFoundException,
            is UpdateStatusTask.UploaderNotFoundException ->
                return "extension not found"
            else -> {
                val cause = ex.cause
                when (cause) {
                    is UpdateStatusTask.ExtensionVersionMismatchException ->
                        return "extension version mismatch"
                    is IOException ->
                        return "io exception"
                    is MicroBlogException -> {
                        if (cause.isCausedByNetworkIssue) {
                            return "network error"
                        }
                        return "request error"
                    }
                }
                when (ex) {
                    is UpdateStatusTask.ShortenException,
                    is UpdateStatusTask.UploadException ->
                        return "extension error"
                }
                return "internal error"
            }
        }
    }

    override val name: String
        get() = "Tweet"

    override fun forEachValues(action: (String, String?) -> Unit) {
        action("Status Type", draftActionTypeString(actionType))
        action("Media Type", parcelableMediaTypeString(mediaType))
        action("Location Type", locationType)
        action("Success", success.toString())

    }

}