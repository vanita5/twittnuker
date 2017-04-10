/*
 *          Twittnuker - Twitter client for Android
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.vanita5.twittnuker.model.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import org.mariotaku.library.objectcursor.converter.CursorFieldConverter;
import de.vanita5.twittnuker.model.Draft;
import de.vanita5.twittnuker.model.draft.ActionExtras;
import de.vanita5.twittnuker.model.draft.SendDirectMessageActionExtras;
import de.vanita5.twittnuker.model.draft.StatusObjectExtras;
import de.vanita5.twittnuker.model.draft.UpdateStatusActionExtras;
import de.vanita5.twittnuker.provider.TwidereDataStore.Drafts;
import de.vanita5.twittnuker.util.JsonSerializer;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

public class DraftExtrasConverter implements CursorFieldConverter<ActionExtras> {
    @Override
    public ActionExtras parseField(Cursor cursor, int columnIndex, ParameterizedType fieldType) throws IOException {
        final String actionType = cursor.getString(cursor.getColumnIndex(Drafts.ACTION_TYPE));
        final String json = cursor.getString(columnIndex);
        if (TextUtils.isEmpty(actionType) || TextUtils.isEmpty(json)) return null;
        switch (actionType) {
            case Draft.Action.UPDATE_STATUS_COMPAT_1:
            case Draft.Action.UPDATE_STATUS_COMPAT_2:
            case Draft.Action.UPDATE_STATUS:
            case Draft.Action.REPLY:
            case Draft.Action.QUOTE: {
                return JsonSerializer.parse(json, UpdateStatusActionExtras.class);
            }
            case Draft.Action.SEND_DIRECT_MESSAGE_COMPAT:
            case Draft.Action.SEND_DIRECT_MESSAGE: {
                return JsonSerializer.parse(json, SendDirectMessageActionExtras.class);
            }
            case Draft.Action.FAVORITE:
            case Draft.Action.RETWEET: {
                return JsonSerializer.parse(json, StatusObjectExtras.class);
            }
        }
        return null;
    }

    @Override
    public void writeField(ContentValues values, ActionExtras object, String columnName, ParameterizedType fieldType) throws IOException {
        if (object == null) return;
        values.put(columnName, JsonSerializer.serialize(object));
    }
}