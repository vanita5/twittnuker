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

package de.vanita5.twittnuker.util.theme

import android.content.Context
import android.content.res.TypedArray
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.VALUE_THEME_NAME_AUTO
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.VALUE_THEME_NAME_DARK
import de.vanita5.twittnuker.util.ThemeUtils


fun getCurrentThemeResource(context: Context, theme: String, fromThemeResource: Int = 0): Int {
    val a: TypedArray
    if (fromThemeResource == 0) {
        a = context.obtainStyledAttributes(R.styleable.TwidereTheme)
    } else {
        a = context.obtainStyledAttributes(fromThemeResource, R.styleable.TwidereTheme)
    }
    try {
        val lightTheme = a.getResourceId(R.styleable.TwidereTheme_lightThemeResource, 0)
        val darkTheme = a.getResourceId(R.styleable.TwidereTheme_darkThemeResource, 0)
        if (lightTheme == 0 || darkTheme == 0) return 0
        return when (theme) {
            VALUE_THEME_NAME_AUTO -> ThemeUtils.getCurrentThemeResource(context, lightTheme, darkTheme)
            VALUE_THEME_NAME_DARK -> darkTheme
            else -> lightTheme
        }
    } finally {
        a.recycle()
    }
}