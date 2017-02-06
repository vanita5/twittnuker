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

package de.vanita5.twittnuker.util


import org.junit.Assert
import org.junit.Test


class UriUtilsTest {

    @Test
    @Throws(Exception::class)
    fun testGetAuthority() {
        Assert.assertEquals("www.google.com", UriUtils.getAuthority("http://www.google.com/"))
        Assert.assertEquals("twitter.com", UriUtils.getAuthority("https://twitter.com"))
        Assert.assertNull(UriUtils.getAuthority("www.google.com/"))
    }

    @Test
    @Throws(Exception::class)
    fun testGetPath() {
        Assert.assertEquals("/", UriUtils.getPath("http://www.example.com/"))
        Assert.assertEquals("", UriUtils.getPath("http://www.example.com"))
        Assert.assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path"))
        Assert.assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path?with=query"))
        Assert.assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/?with=query"))
        Assert.assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path?with=query#fragment"))
        Assert.assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/?with=query#fragment"))
        Assert.assertEquals("/test/path", UriUtils.getPath("https://example.com/test/path#fragment"))
        Assert.assertEquals("/test/path/", UriUtils.getPath("https://example.com/test/path/#fragment"))
    }
}