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

import android.content.Context;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;

public class CustomAPIConfig implements Constants {

    String name;
    String apiUrlFormat;
    int authType;
    boolean sameOAuthUrl;
    boolean noVersionSuffix;
    String consumerKey;
    String consumerSecret;

    public CustomAPIConfig(String name, String apiUrlFormat, int authType, boolean sameOAuthUrl,
                           boolean noVersionSuffix, String consumerKey, String consumerSecret) {
        this.name = name;
        this.apiUrlFormat = apiUrlFormat;
        this.authType = authType;
        this.sameOAuthUrl = sameOAuthUrl;
        this.noVersionSuffix = noVersionSuffix;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public String getName() {
        return name;
    }

    public String getApiUrlFormat() {
        return apiUrlFormat;
    }

    public int getAuthType() {
        return authType;
    }

    public boolean isSameOAuthUrl() {
        return sameOAuthUrl;
    }

    public boolean isNoVersionSuffix() {
        return noVersionSuffix;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    @NonNull
    public static CustomAPIConfig[] listDefault(@NonNull Context context) {
        CustomAPIConfig[] list = new CustomAPIConfig[2];
        list[0] = new CustomAPIConfig(context.getString(R.string.provider_default),
                DEFAULT_TWITTER_API_URL_FORMAT, ParcelableCredentials.AUTH_TYPE_OAUTH, true, false,
                TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
        list[1] = new CustomAPIConfig(context.getString(R.string.provider_fanfou),
                DEFAULT_FANFOU_API_URL_FORMAT, ParcelableCredentials.AUTH_TYPE_OAUTH, true, true,
                FANFOU_CONSUMER_KEY, FANFOU_CONSUMER_SECRET);
        return list;
    }
}