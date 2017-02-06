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

import org.junit.Test

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class TwidereMathUtilsTest {

    @Throws(Exception::class)
    fun testClamp() {

    }

    @Throws(Exception::class)
    fun testClamp1() {

    }

    @Throws(Exception::class)
    fun testNextPowerOf2() {

    }

    @Throws(Exception::class)
    fun testPrevPowerOf2() {

    }

    @Throws(Exception::class)
    fun testSum() {

    }

    @Throws(Exception::class)
    fun testSum1() {

    }

    @Throws(Exception::class)
    fun testSum2() {

    }

    @Test
    fun testInRange() {
        assertTrue(TwidereMathUtils.inRange(5, 0, 10, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(0, 0, 10, TwidereMathUtils.RANGE_EXCLUSIVE_EXCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(0, 5, 10, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(5, 5, 10, TwidereMathUtils.RANGE_EXCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(10, 5, 10, TwidereMathUtils.RANGE_INCLUSIVE_EXCLUSIVE))
    }

    @Test
    fun testInRange1() {
        assertTrue(TwidereMathUtils.inRange(5f, 0f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(0f, 0f, 10f, TwidereMathUtils.RANGE_EXCLUSIVE_EXCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(0f, 5f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(5f, 5f, 10f, TwidereMathUtils.RANGE_EXCLUSIVE_INCLUSIVE))
        assertFalse(TwidereMathUtils.inRange(10f, 5f, 10f, TwidereMathUtils.RANGE_INCLUSIVE_EXCLUSIVE))
    }
}