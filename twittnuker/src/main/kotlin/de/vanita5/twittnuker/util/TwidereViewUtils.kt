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

import android.graphics.RectF
import android.support.annotation.UiThread
import android.view.MotionEvent
import android.view.View

object TwidereViewUtils {

    private val location = IntArray(2)
    private val rect = RectF()

    @UiThread
    fun hitView(event: MotionEvent, view: View): Boolean {
        view.getLocationOnScreen(location)
        rect.set(location[0].toFloat(), location[1].toFloat(), location[0].toFloat() + view.width,
                location[1].toFloat() + view.height)
        return rect.contains(event.rawX, event.rawY)
    }
}