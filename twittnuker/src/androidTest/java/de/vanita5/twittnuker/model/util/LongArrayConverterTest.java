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

package de.vanita5.twittnuker.model.util;

import android.database.MatrixCursor;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

public class LongArrayConverterTest {
    private final LongArrayCursorFieldConverter converter = new LongArrayCursorFieldConverter();

    @Test
    public void testParseField() throws Exception {
        MatrixCursor cursor = new MatrixCursor(new String[]{"a", "b", "c", "d"});
        cursor.addRow(new String[]{"1,2,3,4", "5,6,7", "8,", ""});
        cursor.moveToFirst();
        assertArrayEquals(new long[]{1, 2, 3, 4}, converter.parseField(cursor, 0, TypeUtils.parameterize(long[].class)));
        assertArrayEquals(new long[]{5, 6, 7}, converter.parseField(cursor, 1, TypeUtils.parameterize(long[].class)));
        assertNull(converter.parseField(cursor, 2, TypeUtils.parameterize(long[].class)));
        assertNull(converter.parseField(cursor, 3, TypeUtils.parameterize(long[].class)));
    }

    @Test
    public void testWriteField() throws Exception {

    }
}