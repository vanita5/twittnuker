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

package de.vanita5.twittnuker.model.message;

import android.os.Parcelable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import de.vanita5.twittnuker.model.ParcelableMessage.MessageType;

import java.io.IOException;


@JsonObject
public abstract class MessageExtras implements Parcelable {
    public static MessageExtras parse(final String messageType, final String json) throws IOException {
        switch (messageType) {
            case MessageType.STICKER:
                return LoganSquare.parse(json, StickerExtras.class);
            case MessageType.JOIN_CONVERSATION:
            case MessageType.PARTICIPANTS_LEAVE:
            case MessageType.PARTICIPANTS_JOIN:
                return LoganSquare.parse(json, UserArrayExtras.class);
        }
        return null;
    }
}