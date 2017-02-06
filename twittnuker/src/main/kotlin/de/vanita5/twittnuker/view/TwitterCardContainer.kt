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

package de.vanita5.twittnuker.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import de.vanita5.twittnuker.util.support.ViewSupport

class TwitterCardContainer(context: Context, attrs: AttributeSet? = null) : ContainerView(context, attrs) {

    private var cardWidth: Int = 0
    private var cardHeight: Int = 0

    fun setCardSize(width: Int, height: Int) {
        cardWidth = width
        cardHeight = height
        if (!ViewSupport.isInLayout(this)) {
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (cardWidth <= 0 || cardHeight <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        val measuredWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        val measuredHeight = Math.round(measuredWidth * (cardHeight / cardWidth.toFloat()))
        val newWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY)
        val newHeightMeasureSpec: Int
        if (measuredHeight != 0) {
            newHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(measuredHeight, View.MeasureSpec.EXACTLY)
        } else {
            newHeightMeasureSpec = heightMeasureSpec
        }
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }
}