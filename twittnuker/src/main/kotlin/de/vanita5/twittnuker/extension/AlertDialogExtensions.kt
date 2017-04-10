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

package de.vanita5.twittnuker.extension

import android.content.DialogInterface.*
import android.content.res.ColorStateList
import android.support.v7.app.AlertDialog
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import de.vanita5.twittnuker.util.ThemeUtils


fun AlertDialog.applyTheme(): AlertDialog {
    val theme = Chameleon.getOverrideTheme(context, ChameleonUtils.getActivity(context))
    val optimalAccent = ThemeUtils.getOptimalAccentColor(theme.colorAccent, theme.colorForeground)
    val buttonColor = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(0)),
            intArrayOf(theme.textColorSecondary, optimalAccent))
    getButton(BUTTON_POSITIVE)?.setTextColor(buttonColor)
    getButton(BUTTON_NEGATIVE)?.setTextColor(buttonColor)
    getButton(BUTTON_NEUTRAL)?.setTextColor(buttonColor)
    return this
}