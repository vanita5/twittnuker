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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by mariotaku on 16/1/23.
 */
public class TwidereMathUtilsTest {

    public void testClamp() throws Exception {

    }

    public void testClamp1() throws Exception {

    }

    public void testNextPowerOf2() throws Exception {

    }

    public void testPrevPowerOf2() throws Exception {

    }

    public void testSum() throws Exception {

    }

    public void testSum1() throws Exception {

    }

    public void testSum2() throws Exception {

    }

    @Test
    public void testInRange() {
        assertTrue(TwidereMathUtils.inRange(5, 0, 10, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(0, 0, 10, TwidereMathUtils.RANGE_EXCLUSIVE_EXCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(0, 5, 10, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(5, 5, 10, TwidereMathUtils.RANGE_EXCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(10, 5, 10, TwidereMathUtils.RANGE_INCLUSIVE_EXCLUSIVE));
    }

    @Test
    public void testInRange1() {
        assertTrue(TwidereMathUtils.inRange(5f, 0f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(0f, 0f, 10f, TwidereMathUtils.RANGE_EXCLUSIVE_EXCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(0f, 5f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(5f, 5f, 10f, TwidereMathUtils.RANGE_EXCLUSIVE_INCLUSIVE));
        assertFalse(TwidereMathUtils.inRange(10f, 5f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_EXCLUSIVE));
    }
}