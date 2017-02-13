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

package de.vanita5.twittnuker.model;

import android.support.annotation.StringDef;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableNoThanks;

import org.mariotaku.commons.objectcursor.LoganSquareCursorFieldConverter;
import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import de.vanita5.twittnuker.model.message.MessageExtras;
import de.vanita5.twittnuker.model.util.MessageExtrasConverter;
import de.vanita5.twittnuker.model.util.UserKeyCursorFieldConverter;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.Messages.Conversations;

import java.util.Arrays;

@JsonObject
@CursorObject(tableInfo = true, valuesCreator = true)
public class ParcelableMessageConversation {
    @CursorField(value = Conversations._ID, type = TwidereDataStore.TYPE_PRIMARY_KEY, excludeWrite = true)
    public long _id;

    @JsonField(name = "account_key")
    @CursorField(value = Conversations.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;
    @JsonField(name = "account_color")
    @CursorField(Conversations.ACCOUNT_COLOR)
    public int account_color;

    @JsonField(name = "conversation_id")
    @CursorField(Conversations.CONVERSATION_ID)
    public String id;

    @ConversationType
    @JsonField(name = "conversation_type")
    @CursorField(Conversations.CONVERSATION_TYPE)
    public String conversation_type;

    @ParcelableMessage.MessageType
    @JsonField(name = "message_type")
    @CursorField(Conversations.MESSAGE_TYPE)
    public String message_type;

    @JsonField(name = "timestamp")
    @CursorField(value = Conversations.MESSAGE_TIMESTAMP)
    public long message_timestamp;

    @JsonField(name = "local_timestamp")
    @CursorField(value = Conversations.LOCAL_TIMESTAMP)
    public long local_timestamp;

    @JsonField(name = "text_unescaped")
    @CursorField(Conversations.TEXT_UNESCAPED)
    public String text_unescaped;

    @JsonField(name = "media")
    @CursorField(value = Conversations.MEDIA, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableMedia[] media;

    @JsonField(name = "spans")
    @CursorField(value = Conversations.SPANS, converter = LoganSquareCursorFieldConverter.class)
    public SpanItem[] spans;

    @JsonField(name = "extras")
    @CursorField(value = Conversations.EXTRAS, converter = MessageExtrasConverter.class)
    public MessageExtras extras;

    @JsonField(name = "extras")
    @ParcelableNoThanks
    ParcelableMessage.InternalExtras internalExtras;

    @JsonField(name = "participants")
    @CursorField(value = Conversations.PARTICIPANTS, converter = LoganSquareCursorFieldConverter.class)
    public ParcelableUser[] participants;

    @JsonField(name = "sender_key")
    @CursorField(value = Conversations.SENDER_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey sender_key;

    @JsonField(name = "recipient_key")
    @CursorField(value = Conversations.RECIPIENT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey recipient_key;

    @JsonField(name = "is_outgoing")
    @CursorField(Conversations.IS_OUTGOING)
    public boolean is_outgoing;

    @JsonField(name = "request_cursor")
    @CursorField(value = Conversations.REQUEST_CURSOR)
    public String request_cursor;

    @Override
    public String toString() {
        return "ParcelableMessageConversation{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", id='" + id + '\'' +
                ", conversation_type='" + conversation_type + '\'' +
                ", message_type='" + message_type + '\'' +
                ", message_timestamp=" + message_timestamp +
                ", local_timestamp=" + local_timestamp +
                ", text_unescaped='" + text_unescaped + '\'' +
                ", media=" + Arrays.toString(media) +
                ", spans=" + Arrays.toString(spans) +
                ", extras=" + extras +
                ", internalExtras=" + internalExtras +
                ", participants=" + Arrays.toString(participants) +
                ", sender_key=" + sender_key +
                ", recipient_key=" + recipient_key +
                ", is_outgoing=" + is_outgoing +
                ", request_cursor='" + request_cursor + '\'' +
                '}';
    }

    @StringDef({ConversationType.ONE_TO_ONE, ConversationType.GROUP})
    public @interface ConversationType {
        String ONE_TO_ONE = "one_to_one";
        String GROUP = "group";
    }

}