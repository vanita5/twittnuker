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

import android.content.SharedPreferences;
import android.support.annotation.WorkerThread;

import com.bluelinelabs.logansquare.JsonMapper;
import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import org.mariotaku.restfu.annotation.method.GET;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.http.RestHttpClient;

import java.io.IOException;

import static de.vanita5.twittnuker.constant.SharedPreferenceConstants.KEY_MEDIA_LINK_COUNTS_IN_STATUS;

@JsonObject
public class DefaultFeatures {

    private final static String REMOTE_SETTINGS_URL = "https://raw.githubusercontent.com/vanita5/twittnuker/master/twittnuker/src/main/assets/data/default_features.json";
    private static final String KEY_DEFAULT_TWITTER_CONSUMER_KEY = "default_twitter_consumer_key";
    private static final String KEY_DEFAULT_TWITTER_CONSUMER_SECRET = "default_twitter_consumer_secret";

    @JsonField(name = "media_link_counts_in_status")
    boolean mediaLinkCountsInStatus = false;

    @JsonField(name = "default_twitter_consumer_key")
    String defaultTwitterConsumerKey;
    @JsonField(name = "default_twitter_consumer_secret")
    String defaultTwitterConsumerSecret;

    public boolean isMediaLinkCountsInStatus() {
        return mediaLinkCountsInStatus;
    }

    public String getDefaultTwitterConsumerKey() {
        return defaultTwitterConsumerKey;
    }

    public String getDefaultTwitterConsumerSecret() {
        return defaultTwitterConsumerSecret;
    }

    @WorkerThread
    public boolean loadRemoteSettings(RestHttpClient client) throws IOException {
        HttpRequest request = new HttpRequest.Builder().method(GET.METHOD).url(REMOTE_SETTINGS_URL).build();
        final HttpResponse response = client.newCall(request).execute();
        try {
            final JsonMapper<DefaultFeatures> mapper = LoganSquare.mapperFor(DefaultFeatures.class);
            final JsonParser jsonParser = LoganSquare.JSON_FACTORY.createParser(response.getBody().stream());
            if (jsonParser.getCurrentToken() == null) {
                jsonParser.nextToken();
            }
            if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
                jsonParser.skipChildren();
                return false;
            }
            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();
                jsonParser.nextToken();
                mapper.parseField(this, fieldName, jsonParser);
                jsonParser.skipChildren();
            }
        } finally {
            response.close();
        }
        return true;
    }


    public void load(SharedPreferences preferences) {
        mediaLinkCountsInStatus = preferences.getBoolean(KEY_MEDIA_LINK_COUNTS_IN_STATUS,
                mediaLinkCountsInStatus);
        defaultTwitterConsumerKey = preferences.getString(KEY_DEFAULT_TWITTER_CONSUMER_KEY, null);
        defaultTwitterConsumerSecret = preferences.getString(KEY_DEFAULT_TWITTER_CONSUMER_SECRET, null);
    }

    public void save(SharedPreferences preferences) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_MEDIA_LINK_COUNTS_IN_STATUS, mediaLinkCountsInStatus);
        editor.putString(KEY_DEFAULT_TWITTER_CONSUMER_KEY, defaultTwitterConsumerKey);
        editor.putString(KEY_DEFAULT_TWITTER_CONSUMER_SECRET, defaultTwitterConsumerSecret);
        editor.apply();
    }
}