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
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.annotation.AuthTypeInt;
import de.vanita5.twittnuker.util.JsonSerializer;
import de.vanita5.twittnuker.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static de.vanita5.twittnuker.TwittnukerConstants.DEFAULT_TWITTER_API_URL_FORMAT;
import static de.vanita5.twittnuker.TwittnukerConstants.TWITTER_CONSUMER_KEY;
import static de.vanita5.twittnuker.TwittnukerConstants.TWITTER_CONSUMER_SECRET;

@JsonObject
public final class CustomAPIConfig {

    @JsonField(name = "name")
    String name;
    @JsonField(name = "localized_name")
    String localizedName;
    @JsonField(name = "api_url_format")
    String apiUrlFormat;
    @AuthTypeInt
    @JsonField(name = "auth_type", typeConverter = AuthTypeConverter.class)
    int authType;
    @JsonField(name = "same_oauth_url")
    boolean sameOAuthUrl;
    @JsonField(name = "no_version_suffix")
    boolean noVersionSuffix;
    @JsonField(name = "consumer_key")
    String consumerKey;
    @JsonField(name = "consumer_secret")
    String consumerSecret;

    CustomAPIConfig() {
    }

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

    public String getLocalizedName(Context context) {
        if (localizedName == null) return name;
        final Resources res = context.getResources();
        int id = res.getIdentifier(localizedName, "string", context.getPackageName());
        if (id != 0) {
            return res.getString(id);
        }
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
    public static List<CustomAPIConfig> listDefault(@NonNull Context context) {
        final AssetManager assets = context.getAssets();
        InputStream is = null;
        try {
            is = assets.open("data/default_api_configs.json");
            List<CustomAPIConfig> configList = JsonSerializer.parseList(is, CustomAPIConfig.class);
            if (configList == null) return listBuiltin(context);
            return configList;
        } catch (IOException e) {
            return listBuiltin(context);
        } finally {
            Utils.closeSilently(is);
        }
    }

    public static List<CustomAPIConfig> listBuiltin(@NonNull Context context) {
        return Collections.singletonList(new CustomAPIConfig(context.getString(R.string.provider_default),
                DEFAULT_TWITTER_API_URL_FORMAT, AuthTypeInt.OAUTH, true, false,
                TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET));
    }

    static class AuthTypeConverter extends StringBasedTypeConverter<Integer> {
        @Override
        @AuthTypeInt
        public Integer getFromString(String string) {
            if (string == null) return AuthTypeInt.OAUTH;
            switch (string) {
                case "oauth": {
                    return AuthTypeInt.OAUTH;
                }
                case "xauth": {
                    return AuthTypeInt.XAUTH;
                }
                case "basic": {
                    return AuthTypeInt.BASIC;
                }
                case "twip_o_mode": {
                    return AuthTypeInt.TWIP_O_MODE;
                }
            }
            return AuthTypeInt.OAUTH;
        }

        @Override
        public String convertToString(@AuthTypeInt Integer object) {
            if (object == null) return "oauth";
            switch (object) {
                case AuthTypeInt.OAUTH: {
                    return "oauth";
                }
                case AuthTypeInt.XAUTH: {
                    return "xauth";
                }
                case AuthTypeInt.BASIC: {
                    return "basic";
                }
                case AuthTypeInt.TWIP_O_MODE: {
                    return "twip_o_mode";
                }
            }
            return "oauth";
        }
    }
}