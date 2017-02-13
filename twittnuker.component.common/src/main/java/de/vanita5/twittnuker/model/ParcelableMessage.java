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

package de.vanita5.twittnuker.model;

import android.support.annotation.StringDef;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import de.vanita5.twittnuker.model.util.UserKeyCursorFieldConverter;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages;

import java.util.Arrays;

@CursorObject(tableInfo = true, valuesCreator = true)
public class ParcelableMessage {
    @CursorField(value = Messages._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    public long _id;

    @CursorField(value = Messages.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;

    @CursorField(Messages.MESSAGE_ID)
    public String id;

    @CursorField(Messages.CONVERSATION_ID)
    public String conversation_id;

    @CursorField(Messages.MESSAGE_TYPE)
    @Type
    public String message_type;

    @CursorField(Messages.MESSAGE_TIMESTAMP)
    public long message_timestamp;

    @CursorField(Messages.LOCAL_TIMESTAMP)
    public long local_timestamp;

    @CursorField(Messages.TEXT_UNESCAPED)
    public String text_unescaped;
    @CursorField(value = Messages.MEDIA, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;
    @CursorField(value = Messages.SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] spans;
    @CursorField(value = Messages.EXTRAS)
    public String extras;

    @CursorField(value = Messages.SENDER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey sender_key;
    @CursorField(value = Messages.RECIPIENT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey recipient_key;

    @CursorField(Messages.IS_OUTGOING)
    public boolean is_outgoing;

    @CursorField(value = Messages.REQUEST_CURSOR)
    public String request_cursor;

    @Override
    public String toString() {
        return "ParcelableMessage{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", id='" + id + '\'' +
                ", conversation_id='" + conversation_id + '\'' +
                ", message_type='" + message_type + '\'' +
                ", message_timestamp=" + message_timestamp +
                ", text_unescaped='" + text_unescaped + '\'' +
                ", media=" + Arrays.toString(media) +
                ", spans=" + Arrays.toString(spans) +
                ", sender_key=" + sender_key +
                ", recipient_key=" + recipient_key +
                ", is_outgoing=" + is_outgoing +
                ", request_cursor='" + request_cursor + '\'' +
                '}';
    }

    @StringDef({Type.TEXT, Type.STICKER})
    public @interface Type {
        String TEXT = "text";
        String STICKER = "sticker";
    }
}