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

package de.vanita5.twittnuker.model.tab.extra;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import de.vanita5.twittnuker.annotation.CustomTabType;

import java.io.IOException;

@JsonObject
public abstract class TabExtras implements Parcelable {
    @CallSuper
    public void copyToBundle(Bundle bundle) {

    }

    /**
     * Remember to make this method correspond to {@code CustomTabUtils#newTabExtras(String)}
     */
    @Nullable
    public static TabExtras parse(@NonNull @CustomTabType String type, @Nullable String json) throws IOException {
        if (json == null) return null;
        switch (type) {
            case CustomTabType.NOTIFICATIONS_TIMELINE: {
                return LoganSquare.parse(json, InteractionsTabExtras.class);
            }
            case CustomTabType.HOME_TIMELINE: {
                return LoganSquare.parse(json, HomeTabExtras.class);
            }
            case CustomTabType.TRENDS_SUGGESTIONS: {
                return LoganSquare.parse(json, TrendsTabExtras.class);
            }
        }
        return null;
    }
}