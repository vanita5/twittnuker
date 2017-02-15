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

import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import de.vanita5.twittnuker.view.iface.IIconActionButton

class PreferencesItemIconView(context: Context, attrs: AttributeSet? = null) : IconActionView(context, attrs) {

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): IIconActionButton.Appearance? {
        val appearance = IIconActionButton.Appearance()
        appearance.activatedColor = ChameleonUtils.getColorDependent(theme.colorControlActivated)
        appearance.defaultColor = theme.colorForeground
        return appearance
    }
}