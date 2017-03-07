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

import android.database.MatrixCursor
import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.library.objectcursor.ObjectCursor
import de.vanita5.twittnuker.model.FiltersData
import de.vanita5.twittnuker.model.ParcelableStatus

@RunWith(AndroidJUnit4::class)
class ObjectCursorTest {

    @Test
    fun testSimple() {
        val cur = MatrixCursor(emptyArray(), 0)
        ObjectCursor.indicesFrom(cur, ParcelableStatus::class.java)
    }

    @Test
    fun testMemberClass() {
        val cur = MatrixCursor(emptyArray(), 0)
        ObjectCursor.indicesFrom(cur, FiltersData.BaseItem::class.java)
    }
}