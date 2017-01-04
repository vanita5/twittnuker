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

package de.vanita5.twittnuker.util.sync

import android.content.SharedPreferences
import android.graphics.Color
import org.mariotaku.ktextension.HexColorFormat
import org.mariotaku.ktextension.toHexColor

object UserColorsSyncProcessor : FileBasedPreferencesValuesSyncHelper.Processor {
    override fun loadValue(map: MutableMap<String, String>, key: String, value: Any?) {
        if (value is Int) {
            map.put(key, toHexColor(value, format = HexColorFormat.RGB))
        }
    }

    override fun saveValue(editor: SharedPreferences.Editor, key: String, value: String) {
        try {
            editor.putInt(key, Color.parseColor(value))
        } catch (e: IllegalArgumentException) {
            // Ignore
        }
    }

    override val whatData: String = "user colors"

    override val snapshotFileName: String = "user_colors.xml"

}