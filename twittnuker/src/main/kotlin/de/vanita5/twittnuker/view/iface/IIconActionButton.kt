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

package de.vanita5.twittnuker.view.iface

import android.content.Context
import android.support.annotation.ColorInt
import android.util.AttributeSet

import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.chameleon.internal.ChameleonTypedArray
import org.mariotaku.chameleon.view.ChameleonTextView
import de.vanita5.twittnuker.R

interface IIconActionButton : ChameleonView {

    var defaultColor: Int
        @ColorInt get @ColorInt set
    var activatedColor: Int
        @ColorInt get @ColorInt set
    var disabledColor: Int
        @ColorInt get @ColorInt set

    class Appearance : ChameleonTextView.Appearance() {
        var defaultColor: Int = 0
            @ColorInt get @ColorInt set
        var activatedColor: Int = 0
            @ColorInt get @ColorInt set
        var disabledColor: Int = 0
            @ColorInt get @ColorInt set

        companion object {

            fun create(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): Appearance {
                val appearance = Appearance()
                val a = ChameleonTypedArray.obtain(context, attributeSet, R.styleable.IconActionButton, theme)
                appearance.defaultColor = a.getColor(R.styleable.IconActionButton_iabColor, 0, false)
                appearance.activatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0, false)
                appearance.disabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0, false)
                a.recycle()
                return appearance
            }

            fun apply(view: IIconActionButton, appearance: Appearance) {
                val defaultColor = appearance.defaultColor
                if (defaultColor != 0) {
                    view.defaultColor = defaultColor
                }
                val activatedColor = appearance.activatedColor
                if (activatedColor != 0) {
                    view.activatedColor = activatedColor
                }
                val disabledColor = appearance.disabledColor
                if (disabledColor != 0) {
                    view.disabledColor = disabledColor
                }
            }
        }
    }
}