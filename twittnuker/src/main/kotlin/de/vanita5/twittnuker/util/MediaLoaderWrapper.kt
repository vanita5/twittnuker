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

package de.vanita5.twittnuker.util

import android.content.SharedPreferences
import org.mariotaku.kpreferences.get
import de.vanita5.twittnuker.constant.mediaPreloadKey
import de.vanita5.twittnuker.constant.mediaPreloadOnWifiOnlyKey
import de.vanita5.twittnuker.model.ParcelableActivity
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.model.util.getActivityStatus

class MediaLoaderWrapper {

    var isNetworkMetered: Boolean = true
    private var preloadEnabled: Boolean = true
    private var preloadOnWifiOnly: Boolean = true

    private val shouldPreload: Boolean get() = preloadEnabled && (!preloadOnWifiOnly || !isNetworkMetered)


    fun preloadStatus(status: ParcelableStatus) {
        if (!shouldPreload) return
        preloadProfileImage(status.user_profile_image_url)
        preloadProfileImage(status.quoted_user_profile_image)
        preloadMedia(status.media)
        preloadMedia(status.quoted_media)
    }

    fun preloadActivity(activity: ParcelableActivity) {
        if (!shouldPreload) return
        activity.getActivityStatus()?.let { preloadStatus(it) }
    }

    fun reloadOptions(preferences: SharedPreferences) {
        preloadEnabled = preferences[mediaPreloadKey]
        preloadOnWifiOnly = preferences[mediaPreloadOnWifiOnlyKey]
    }

    private fun preloadMedia(media: Array<ParcelableMedia>?) {
        media?.forEach { item ->
            val url = item.preview_url ?: item.media_url ?: return@forEach
            preloadPreviewImage(url)
        }
    }

    private fun preloadProfileImage(url: String?) {
    }

    private fun preloadPreviewImage(url: String?) {
    }

}