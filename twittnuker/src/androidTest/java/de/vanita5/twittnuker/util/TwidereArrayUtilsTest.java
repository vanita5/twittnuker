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

package de.vanita5.twittnuker.util;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class TwidereArrayUtilsTest {

    @Test
    public void testMergeArray() throws Exception {
        String[] array1 = {"1", "2"};
        String[] array2 = {"1", "2"};
        String[] array3 = null;

        //noinspection ConstantConditions
        String[] merged = new String[TwidereArrayUtils.arraysLength(array1, array2, array3)];
        //noinspection ConstantConditions
        TwidereArrayUtils.mergeArray(merged, array1, array2, array3);
        String[] expected = {"1", "2", "1", "2"};
        assertArrayEquals(expected, merged);
    }

    @Test
    public void testArraysLength() throws Exception {
        String[] array1 = {"1", "2"};
        String[] array2 = {"1", "2"};
        String[] array3 = null;
        //noinspection ConstantConditions
        assertEquals(4, TwidereArrayUtils.arraysLength(array1, array2, array3));
        assertEquals(6, TwidereArrayUtils.arraysLength(array1, array2, array2));
    }
}