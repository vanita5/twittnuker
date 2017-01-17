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

import okhttp3.HttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MicroBlogAPIFactoryTest {

    @Test
    @Throws(Exception::class)
    fun testGetApiUrl() {
        assertEquals("https://api.twitter.com/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", null))
        assertEquals("https://api.twitter.com/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", null))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1/"))
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "1.1"))
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1"))
        assertEquals("https://api.twitter.com/1.1", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "/1.1"))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "1.1/"))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "1.1/"))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com", "api", "/1.1/"))
        assertEquals("https://api.twitter.com/1.1/", MicroBlogAPIFactory.getApiUrl("https://[DOMAIN.]twitter.com/", "api", "/1.1/"))
    }

    @Test
    fun testGetApiBaseUrl() {
        assertEquals("https://media.twitter.com", MicroBlogAPIFactory.getApiBaseUrl("https://api.twitter.com", "media"))
        assertNotNull(HttpUrl.parse(MicroBlogAPIFactory.getApiBaseUrl("https://[invalid]twitter.com/", "api")))
    }
}