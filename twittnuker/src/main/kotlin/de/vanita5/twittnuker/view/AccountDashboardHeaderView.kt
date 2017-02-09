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

package de.vanita5.twittnuker.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import de.vanita5.twittnuker.R

class AccountDashboardHeaderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private var sizeMeasurementId: Int

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AccountDashboardHeaderView)
        sizeMeasurementId = a.getResourceId(R.styleable.AccountDashboardHeaderView_sizeMeasurementId, 0)
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val children: List<View> = (0 until childCount).map { getChildAt(it) }
        val sizeMeasurementView = children.find { it.id == sizeMeasurementId }!!
        measureChild(sizeMeasurementView, widthMeasureSpec, heightMeasureSpec)
        children.forEach { child ->
            if (child.id != sizeMeasurementId) {
                measureChild(child, MeasureSpec.makeMeasureSpec(sizeMeasurementView.measuredWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(sizeMeasurementView.measuredHeight, MeasureSpec.EXACTLY))
            }
        }
        setMeasuredDimension(sizeMeasurementView.measuredWidth, sizeMeasurementView.measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        (0 until childCount).forEach {
            getChildAt(it).layout(0, 0, r - l, b - t)
        }
    }

}