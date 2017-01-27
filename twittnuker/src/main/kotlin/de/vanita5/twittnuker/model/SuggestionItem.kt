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

package de.vanita5.twittnuker.model

import android.database.Cursor
import de.vanita5.twittnuker.provider.TwidereDataStore
import de.vanita5.twittnuker.provider.TwidereDataStore.*

class SuggestionItem(cursor: Cursor, indices: Indices) {

    val title: String?
    val summary: String?
    val _id: Long
    val extra_id: String?

    init {
        _id = cursor.getLong(indices._id)
        title = cursor.getString(indices.title)
        summary = cursor.getString(indices.summary)
        extra_id = cursor.getString(indices.extra_id)
    }

    class Indices(cursor: Cursor) {
        val _id: Int = cursor.getColumnIndex(Suggestions._ID)
        val type: Int = cursor.getColumnIndex(Suggestions.TYPE)
        val title: Int = cursor.getColumnIndex(Suggestions.TITLE)
        val value: Int = cursor.getColumnIndex(Suggestions.VALUE)
        val summary: Int = cursor.getColumnIndex(Suggestions.SUMMARY)
        val icon: Int = cursor.getColumnIndex(Suggestions.ICON)
        val extra_id: Int = cursor.getColumnIndex(Suggestions.EXTRA_ID)

    }
}