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

package de.vanita5.twittnuker.model.util;

import android.content.ContentValues;
import android.database.Cursor;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import de.vanita5.twittnuker.model.AccountKey;

import java.lang.reflect.ParameterizedType;

public class AccountKeyCursorFieldConverter implements CursorFieldConverter<AccountKey> {
    @Override
    public AccountKey parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        return AccountKey.valueOf(cursor.getString(columnIndex));
    }

    @Override
    public void writeField(ContentValues values, AccountKey object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        values.put(columnName, object.toString());
    }

}