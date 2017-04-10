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

package de.vanita5.twittnuker.model.util

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import de.vanita5.twittnuker.library.twitter.model.Status
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.test.R
import de.vanita5.twittnuker.util.JsonSerializer

@RunWith(AndroidJUnit4::class)
class ParcelableStatusUtilsTest {

    val expectedStatusText = "Yalp Store (Download apks from Google Play Store). !gnusocial\n\nhttps://f-droid.org/app/com.github.yeriomin.yalpstore"

    @Test
    fun testFromStatus() {
        val context = InstrumentationRegistry.getContext()
        val status_8754050 = context.resources.openRawResource(R.raw.status_8754050).use {
            val status = JsonSerializer.parse(it, Status::class.java)
            return@use ParcelableStatusUtils.fromStatus(status, UserKey("1234567", "gnusocial.de"),
                    "statusnet", false)
        }

        val status_9171447 = context.resources.openRawResource(R.raw.status_9171447).use {
            val status = JsonSerializer.parse(it, Status::class.java)
            return@use ParcelableStatusUtils.fromStatus(status, UserKey("1234567", "gnusocial.de"),
                    "statusnet", false)
        }

        Assert.assertEquals(status_8754050.text_unescaped, expectedStatusText)
        Assert.assertEquals(status_9171447.text_unescaped, expectedStatusText)
    }
}