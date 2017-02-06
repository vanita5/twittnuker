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

package de.vanita5.twittnuker.test.provider

import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import de.vanita5.twittnuker.provider.TwidereDataStore


@RunWith(AndroidJUnit4::class)
class TwidereDataStoreTest {
    @Test
    fun testBaseUris() {
        val context = InstrumentationRegistry.getTargetContext()
        val resolver = context.contentResolver
        Assert.assertEquals(TwidereDataStore.BASE_CONTENT_URI, Uri.parse("content://twidere"))
        Assert.assertNull(resolver.query(TwidereDataStore.CONTENT_URI_NULL, null, null, null, null))
        Assert.assertNotNull(resolver.query(TwidereDataStore.CONTENT_URI_EMPTY, null, null, null, null))
    }
}