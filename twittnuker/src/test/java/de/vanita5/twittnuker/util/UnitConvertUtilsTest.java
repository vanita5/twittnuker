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

import static org.junit.Assert.assertEquals;

public class UnitConvertUtilsTest {

    @Test
    public void testCalculateProperCount() throws Exception {
        assertEquals("201", UnitConvertUtils.calculateProperCount(201));
        assertEquals("2.2 K", UnitConvertUtils.calculateProperCount(2201));
        assertEquals("2.1 K", UnitConvertUtils.calculateProperCount(2100));
        assertEquals("2 K", UnitConvertUtils.calculateProperCount(2000));
        assertEquals("2 K", UnitConvertUtils.calculateProperCount(2049));
        assertEquals("2.1 K", UnitConvertUtils.calculateProperCount(2050));
        assertEquals("2.1 K", UnitConvertUtils.calculateProperCount(2099));
        assertEquals("2.4 K", UnitConvertUtils.calculateProperCount(2430));
        assertEquals("2.5 K", UnitConvertUtils.calculateProperCount(2499));
        assertEquals("2.4 K", UnitConvertUtils.calculateProperCount(2449));
        assertEquals("2.5 K", UnitConvertUtils.calculateProperCount(2450));
    }
}