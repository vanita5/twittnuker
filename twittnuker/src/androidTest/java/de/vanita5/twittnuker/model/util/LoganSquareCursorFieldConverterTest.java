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
import android.database.MatrixCursor;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;
import de.vanita5.twittnuker.util.JsonSerializer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class LoganSquareCursorFieldConverterTest {
    private final LoganSquareCursorFieldConverter converter = new LoganSquareCursorFieldConverter();

    private final Model jsonObject = new Model("a");
    private final Model[] jsonArray = {new Model("a"), new Model("b"), new Model("c")};
    private final List<Model> jsonList = Arrays.asList(jsonArray);
    private final Map<String, Model> jsonMap = Collections.singletonMap("key", new Model("value"));

    @Test
    public void testParseField() throws Exception {
        final String[] columns = {"json_object", "json_array", "json_list", "json_map"};
        MatrixCursor cursor = new MatrixCursor(columns);
        cursor.addRow(new String[]{
                JsonSerializer.serialize(jsonObject, Model.class),
                JsonSerializer.serialize(jsonArray, Model.class),
                JsonSerializer.serialize(jsonList, Model.class),
                JsonSerializer.serialize(jsonMap, Model.class)
        });
        cursor.moveToFirst();
        assertEquals(jsonObject, converter.parseField(cursor, 0, TypeUtils.parameterize(Model.class)));
        assertArrayEquals(jsonArray, (Model[]) converter.parseField(cursor, 1, TypeUtils.parameterize(Model[].class)));
        assertEquals((Object) jsonList, converter.parseField(cursor, 2, TypeUtils.parameterize(List.class, Model.class)));
        assertEquals((Object) jsonMap, converter.parseField(cursor, 3, TypeUtils.parameterize(Map.class, String.class, Model.class)));
    }

    @Test
    public void testWriteField() throws Exception {
        final ContentValues contentValues = new ContentValues();
        converter.writeField(contentValues, jsonObject, "json_object", TypeUtils.parameterize(Model.class));
        converter.writeField(contentValues, jsonArray, "json_array", TypeUtils.parameterize(Model[].class));
        converter.writeField(contentValues, jsonList, "json_list", TypeUtils.parameterize(List.class, Model.class));
        converter.writeField(contentValues, jsonMap, "json_map", TypeUtils.parameterize(Map.class, String.class, Model.class));

        assertEquals(JsonSerializer.serialize(jsonObject, Model.class), contentValues.getAsString("json_object"));
        assertEquals(JsonSerializer.serialize(jsonArray, Model.class), contentValues.getAsString("json_array"));
        assertEquals(JsonSerializer.serialize(jsonList, Model.class), contentValues.getAsString("json_list"));
        assertEquals(JsonSerializer.serialize(jsonMap, Model.class), contentValues.getAsString("json_map"));
    }

    @JsonObject
    public static class Model {
        @JsonField(name = "field")
        String field;

        public Model() {

        }

        public Model(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Model model = (Model) o;

            return !(field != null ? !field.equals(model.field) : model.field != null);

        }

        @Override
        public int hashCode() {
            return field != null ? field.hashCode() : 0;
        }
    }
}