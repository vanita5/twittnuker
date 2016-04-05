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
import android.text.TextUtils;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;

import de.vanita5.twittnuker.model.tab.argument.TabArguments;
import de.vanita5.twittnuker.provider.TwidereDataStore.Tabs;
import de.vanita5.twittnuker.util.CustomTabUtils;
import de.vanita5.twittnuker.util.JsonSerializer;

import java.lang.reflect.ParameterizedType;

public class TabArgumentsFieldConverter implements CursorFieldConverter<TabArguments> {

    @Override
    public TabArguments parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) {
        final String tabType = CustomTabUtils.getTabTypeAlias(cursor.getString(cursor.getColumnIndex(Tabs.TYPE)));
        if (TextUtils.isEmpty(tabType)) return null;
        return CustomTabUtils.parseTabArguments(tabType, cursor.getString(columnIndex));
    }

    @Override
    public void writeField(ContentValues values, TabArguments object, String columnName, ParameterizedType fieldType) {
        if (object == null) return;
        values.put(columnName, JsonSerializer.serialize(object));
    }
}