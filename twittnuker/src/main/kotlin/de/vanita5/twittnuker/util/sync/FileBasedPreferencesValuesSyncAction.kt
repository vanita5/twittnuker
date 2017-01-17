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

import android.content.Context
import android.content.SharedPreferences
import java.io.Closeable
import java.util.*

abstract class FileBasedPreferencesValuesSyncAction<DownloadSession : Closeable, UploadSession : Closeable>(
        context: Context,
        var preferences: SharedPreferences,
        val processor: Processor
) : FileBasedKeyValueSyncAction<DownloadSession, UploadSession>(context) {

    override final val snapshotFileName: String = processor.snapshotFileName

    override final val whatData: String = processor.whatData

    override final fun MutableMap<String, String>.saveToLocal() {
        val editor = preferences.edit()
        editor.clear()
        for ((k, v) in this) {
            processor.saveValue(editor, k, v)
        }
        editor.apply()
    }

    override final fun loadFromLocal(): MutableMap<String, String> {
        val result = HashMap<String, String>()
        for ((k, v) in preferences.all) {
            processor.loadValue(result, k, v)
        }
        return result
    }

    interface Processor {
        val snapshotFileName: String
        val whatData: String
        fun saveValue(editor: SharedPreferences.Editor, key: String, value: String)
        fun loadValue(map: MutableMap<String, String>, key: String, value: Any?)
    }
}