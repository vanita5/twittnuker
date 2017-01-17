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

package de.vanita5.twittnuker.util.media.preview.provider

import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import de.vanita5.twittnuker.model.ParcelableMedia

@RunWith(AndroidJUnit4::class)
class InstagramProviderTest {

    internal val provider = InstagramProvider()

    @Test
    fun testFrom() {
        var media: ParcelableMedia ? = provider.from("https://www.instagram.com/p/abcd1234")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234/")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234?key=value")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234/?key=value")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234/?key=value#fragment")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234#fragment")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/p/abcd1234/#fragment")
        assertEquals("https://instagram.com/p/abcd1234/media/?size=l", media!!.media_url)
        media = provider.from("https://www.instagram.com/user")
        assertNull(media)
    }

}