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

package de.vanita5.twittnuker.model


class ItemCounts(counts: Int) {
    private val data: IntArray = IntArray(counts)

    fun getItemCountIndex(itemPosition: Int): Int {
        var sum = 0
        for (i in data.indices) {
            sum += data[i]
            if (itemPosition < sum) {
                return i
            }
        }
        return -1
    }

    fun getItemStartPosition(countIndex: Int): Int {
        var sum = 0
        for (i in 0..countIndex - 1) {
            sum += data[i]
        }
        return sum
    }

    val itemCount: Int get() = data.sum()

    val size: Int get() = data.size

    operator fun set(countIndex: Int, value: Int) {
        data[countIndex] = value
    }

}