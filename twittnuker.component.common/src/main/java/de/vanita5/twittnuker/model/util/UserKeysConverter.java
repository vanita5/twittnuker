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

import com.bluelinelabs.logansquare.typeconverters.TypeConverter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import de.vanita5.twittnuker.model.UserKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserKeysConverter implements TypeConverter<UserKey[]> {
    @Override
    public UserKey[] parse(JsonParser jsonParser) throws IOException {
        if (jsonParser.getCurrentToken() == null) {
            jsonParser.nextToken();
        }
        if (jsonParser.getCurrentToken() != JsonToken.START_ARRAY) {
            jsonParser.skipChildren();
            return null;
        }
        List<UserKey> list = new ArrayList<>();
        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            list.add(UserKey.valueOf(jsonParser.getValueAsString()));
        }
        return list.toArray(new UserKey[list.size()]);
    }

    @Override
    public void serialize(UserKey[] object, String fieldName, boolean writeFieldNameForObject, JsonGenerator jsonGenerator) throws IOException {
        if (writeFieldNameForObject) {
            jsonGenerator.writeFieldName(fieldName);
        }
        if (object == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeStartArray();
            for (UserKey userKey : object) {
                jsonGenerator.writeString(userKey.toString());
            }
            jsonGenerator.writeEndArray();
        }
    }
}