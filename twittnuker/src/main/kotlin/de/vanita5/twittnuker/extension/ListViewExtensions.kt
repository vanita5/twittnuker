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

package de.vanita5.twittnuker.extension

import android.view.Menu
import android.widget.ListView
import org.mariotaku.ktextension.setItemAvailability
import de.vanita5.twittnuker.R

fun ListView.selectAll() {
    for (i in 0 until count) {
        setItemChecked(i, true)
    }
}

fun ListView.selectNone() {
    for (i in 0 until count) {
        setItemChecked(i, false)
    }
}

fun ListView.invertSelection() {
    val positions = checkedItemPositions
    for (i in 0 until count) {
        setItemChecked(i, !positions.get(i))
    }
}

fun ListView.updateSelectionItems(menu: Menu) {
    val checkedCount = checkedItemCount
    val listCount = count
    menu.setItemAvailability(R.id.select_none, checkedCount > 0)
    menu.setItemAvailability(R.id.select_all, checkedCount < listCount)
    menu.setItemAvailability(R.id.invert_selection, checkedCount > 0 && checkedCount < listCount)
}