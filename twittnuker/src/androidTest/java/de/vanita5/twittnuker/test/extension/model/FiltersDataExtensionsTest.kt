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

package de.vanita5.twittnuker.test.extension.model

import android.support.test.runner.AndroidJUnit4
import android.util.Xml
import de.vanita5.twittnuker.extension.model.parse
import de.vanita5.twittnuker.extension.model.serialize
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.model.UserKey
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class FiltersDataExtensionsTest {
    @Test
    fun testXmlSerialization() {
        val filters = FiltersData().apply {
            users = listOf(userItem(UserKey("123456", "twitter.com"), "name", "screen_name"))
            links = listOf(baseItem("twitter.com"))
            keywords = listOf(baseItem("Keyword"))
            sources = listOf(baseItem("Spam Client"))
        }
        val serializer = Xml.newSerializer()
        val baos = ByteArrayOutputStream()
        serializer.setOutput(baos, "UTF-8")
        filters.serialize(serializer)
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(baos.toByteArray()), "UTF-8")
        val newFilters = FiltersData()
        newFilters.parse(parser)

        Assert.assertEquals(filters.users, newFilters.users)
        Assert.assertEquals(filters.keywords, newFilters.keywords)
        Assert.assertEquals(filters.sources, newFilters.sources)
        Assert.assertEquals(filters.links, newFilters.links)
    }

    private fun baseItem(value: String): FiltersData.BaseItem {
        return FiltersData.BaseItem().apply {
            this.value = value
        }
    }

    private fun userItem(key: UserKey, name: String, screenName: String): FiltersData.UserItem {
        return FiltersData.UserItem().apply {
            this.userKey = key
            this.name = name
            this.screenName = screenName
        }
    }
}