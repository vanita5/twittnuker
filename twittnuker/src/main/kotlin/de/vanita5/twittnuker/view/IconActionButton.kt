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
import android.content.res.ColorStateList
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.view.ViewCompat
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonView
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.util.ThemeUtils
import de.vanita5.twittnuker.view.iface.IIconActionButton

class IconActionButton(
        context: Context, attrs: AttributeSet? = null
) : AppCompatImageButton(context, attrs, R.attr.imageButtonStyle), IIconActionButton {

    override var defaultColor: Int = 0
        @ColorInt
        get() {
            if (field == 0) {
                val color = ViewCompat.getBackgroundTintList(this)
                if (color != null) {
                    val currentColor = color.getColorForState(drawableState, 0)
                    return ThemeUtils.getContrastColor(currentColor, Color.BLACK, Color.WHITE)
                }
            }
            return field
        }
        set(@ColorInt defaultColor) {
            field = defaultColor
            updateColorFilter()
        }

    override var activatedColor: Int = 0
        @ColorInt
        get() {
            if (field != 0) return field
            return defaultColor
        }
        set(@ColorInt activatedColor) {
            field = activatedColor
            updateColorFilter()
        }

    override var disabledColor: Int = 0
        @ColorInt
        get() {
            if (field != 0) return field
            return defaultColor
        }
        set(@ColorInt disabledColor) {
            field = disabledColor
            updateColorFilter()
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton,
                R.attr.cardActionButtonStyle, R.style.Widget_CardActionButton)
        defaultColor = a.getColor(R.styleable.IconActionButton_iabColor, 0)
        activatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0)
        disabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0)
        a.recycle()
        updateColorFilter()
    }

    override fun setActivated(activated: Boolean) {
        super.setActivated(activated)
        updateColorFilter()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        updateColorFilter()
    }

    private fun updateColorFilter() {
        if (isActivated) {
            setColorFilter(activatedColor)
        } else if (isEnabled) {
            setColorFilter(defaultColor)
        } else {
            setColorFilter(disabledColor)
        }
    }

    override fun setBackgroundTintList(tint: ColorStateList?) {
        super.setBackgroundTintList(tint)
        updateColorFilter()
    }

    override fun isPostApplyTheme(): Boolean {
        return false
    }

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): IIconActionButton.Appearance? {
        return IIconActionButton.Appearance.create(context, attributeSet, theme)
    }

    override fun applyAppearance(appearance: ChameleonView.Appearance) {
        IIconActionButton.Appearance.apply(this, appearance as IIconActionButton.Appearance)
    }
}