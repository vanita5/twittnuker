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

package de.vanita5.twittnuker.activity

import android.content.Context
import android.os.Bundle
import org.mariotaku.kpreferences.get
import org.mariotaku.pickncrop.library.MediaPickerActivity
import de.vanita5.twittnuker.TwittnukerConstants.SHARED_PREFERENCES_NAME
import de.vanita5.twittnuker.constant.themeKey
import de.vanita5.twittnuker.util.RestFuNetworkStreamDownloader
import de.vanita5.twittnuker.util.theme.getCurrentThemeResource

class ThemedMediaPickerActivity : MediaPickerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val themeResource = getCurrentThemeResource(this, prefs[themeKey])
        if (themeResource != 0) {
            setTheme(themeResource)
        }
        super.onCreate(savedInstanceState)
    }

    companion object {

        fun withThemed(context: Context): MediaPickerActivity.IntentBuilder {
            val builder = MediaPickerActivity.IntentBuilder(context, ThemedMediaPickerActivity::class.java)
            builder.cropImageActivityClass(ImageCropperActivity::class.java)
            builder.streamDownloaderClass(RestFuNetworkStreamDownloader::class.java)
            return builder
        }
    }

}