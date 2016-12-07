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

package de.vanita5.twittnuker.test.extension

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import de.vanita5.twittnuker.extension.readMimeMessageFrom
import de.vanita5.twittnuker.extension.writeMimeMessageTo
import de.vanita5.twittnuker.model.Draft
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.model.ParcelableMediaUpdate
import de.vanita5.twittnuker.model.UserKey
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class DraftExtensionsTest {
    @Test
    fun testMimeMessageProcessing() {
        val context = InstrumentationRegistry.getTargetContext()
        val draft = Draft()
        draft.timestamp = System.currentTimeMillis()
        draft.account_keys = arrayOf(UserKey("user1", "twitter.com"), UserKey("user2", "twitter.com"))
        draft.text = "Hello world"
        draft.media = arrayOf(ParcelableMediaUpdate().apply {
            this.uri = "file:///system/media/audio/ringtones/Atria.ogg"
            this.type = ParcelableMedia.Type.VIDEO
            this.alt_text = String(CharArray(420).apply {
                fill('A')
            })
        })
        val output = ByteArrayOutputStream()
        draft.writeMimeMessageTo(context, output)
        val input = ByteArrayInputStream(output.toByteArray())

        val newDraft = Draft()
        newDraft.readMimeMessageFrom(context, input)

        Assert.assertArrayEquals(draft.account_keys?.sortedArray(), newDraft.account_keys?.sortedArray())
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(draft.timestamp), TimeUnit.MILLISECONDS.toSeconds(newDraft.timestamp))
        Assert.assertEquals(draft.text, newDraft.text)
        draft.media?.forEachIndexed { idx, expected ->
            val actual = newDraft.media!![idx]
            Assert.assertEquals(expected.alt_text, actual.alt_text)
            Assert.assertEquals(expected.type, actual.type)
        }
    }
}