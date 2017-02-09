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

package de.vanita5.twittnuker.graphic

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.support.v7.graphics.drawable.DrawableWrapper


class BadgeDrawable(drawable: Drawable, color: Int, val badgeSize: Int) : DrawableWrapper(drawable) {

    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
    }
    private val badgeBounds = RectF()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawOval(badgeBounds, badgePaint)
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        badgeBounds.set((bounds.right - badgeSize).toFloat(), 0f, bounds.right.toFloat(), badgeSize.toFloat())
    }
}