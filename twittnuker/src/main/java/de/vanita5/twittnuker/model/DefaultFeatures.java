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

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class DefaultFeatures {

    @JsonField(name = "default_twitter_consumer_key")
    String defaultTwitterConsumerKey;

    @JsonField(name = "default_twitter_consumer_secret")
    String defaultTwitterConsumerSecret;

    @JsonField(name = "twitter_direct_message_media_limit")
    long twitterDirectMessageMediaLimit = 1;

    @JsonField(name = "twitter_direct_message_max_participants")
    long twitterDirectMessageMaxParticipants = 50;

    public String getDefaultTwitterConsumerKey() {
        return defaultTwitterConsumerKey;
    }

    public void setDefaultTwitterConsumerKey(String defaultTwitterConsumerKey) {
        this.defaultTwitterConsumerKey = defaultTwitterConsumerKey;
    }

    public String getDefaultTwitterConsumerSecret() {
        return defaultTwitterConsumerSecret;
    }

    public void setDefaultTwitterConsumerSecret(String defaultTwitterConsumerSecret) {
        this.defaultTwitterConsumerSecret = defaultTwitterConsumerSecret;
    }

    public long getTwitterDirectMessageMediaLimit() {
        return twitterDirectMessageMediaLimit;
    }

    public void setTwitterDirectMessageMediaLimit(long twitterDirectMessageMediaLimit) {
        this.twitterDirectMessageMediaLimit = twitterDirectMessageMediaLimit;
    }

    public long getTwitterDirectMessageMaxParticipants() {
        return twitterDirectMessageMaxParticipants;
    }

    public void setTwitterDirectMessageMaxParticipants(long twitterDirectMessageMaxParticipants) {
        this.twitterDirectMessageMaxParticipants = twitterDirectMessageMaxParticipants;
    }
}