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

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import org.mariotaku.restfu.RestFuUtils;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.annotation.AccountType;
import de.vanita5.twittnuker.model.account.cred.Credentials;
import de.vanita5.twittnuker.util.JsonSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static de.vanita5.twittnuker.TwittnukerConstants.DEFAULT_TWITTER_API_URL_FORMAT;
import static de.vanita5.twittnuker.TwittnukerConstants.TWITTER_CONSUMER_KEY;
import static de.vanita5.twittnuker.TwittnukerConstants.TWITTER_CONSUMER_SECRET;

@ParcelablePlease
@JsonObject
public final class CustomAPIConfig implements Parcelable {

    @JsonField(name = "name")
    String name;
    @AccountType
    @JsonField(name = "type")
    @Nullable
    String type;
    @JsonField(name = "localized_name")
    String localizedName;
    @JsonField(name = "api_url_format")
    @Nullable
    String apiUrlFormat;
    @Credentials.Type
    @JsonField(name = "auth_type")
    String credentialsType;
    @JsonField(name = "same_oauth_url")
    boolean sameOAuthUrl;
    @JsonField(name = "no_version_suffix")
    boolean noVersionSuffix;
    @JsonField(name = "consumer_key")
    @Nullable
    String consumerKey;
    @JsonField(name = "consumer_secret")
    @Nullable
    String consumerSecret;
    @Nullable
    @JsonField(name = "sign_up_url")
    String signUpUrl;

    boolean isDefault;

    public CustomAPIConfig() {
    }

    public CustomAPIConfig(@NonNull String name, @Nullable String type, @Nullable String apiUrlFormat,
            String credentialsType, boolean sameOAuthUrl, boolean noVersionSuffix,
            @Nullable String consumerKey, @Nullable String consumerSecret) {
        this.name = name;
        this.type = type;
        this.apiUrlFormat = apiUrlFormat;
        this.credentialsType = credentialsType;
        this.sameOAuthUrl = sameOAuthUrl;
        this.noVersionSuffix = noVersionSuffix;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    @Nullable
    public String getApiUrlFormat() {
        return apiUrlFormat;
    }

    public String getCredentialsType() {
        return credentialsType;
    }

    public boolean isSameOAuthUrl() {
        return sameOAuthUrl;
    }

    public boolean isNoVersionSuffix() {
        return noVersionSuffix;
    }

    @Nullable
    public String getConsumerKey() {
        return consumerKey;
    }

    @Nullable
    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setApiUrlFormat(@Nullable String apiUrlFormat) {
        this.apiUrlFormat = apiUrlFormat;
    }

    public void setConsumerKey(@Nullable String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public void setConsumerSecret(@Nullable String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public void setCredentialsType(String credentialsType) {
        this.credentialsType = credentialsType;
    }

    public void setSameOAuthUrl(boolean sameOAuthUrl) {
        this.sameOAuthUrl = sameOAuthUrl;
    }

    public void setNoVersionSuffix(boolean noVersionSuffix) {
        this.noVersionSuffix = noVersionSuffix;
    }

    @Nullable
    public String getSignUpUrl() {
        return signUpUrl;
    }

    public void setSignUpUrl(@Nullable String signUpUrl) {
        this.signUpUrl = signUpUrl;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        CustomAPIConfigParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomAPIConfig that = (CustomAPIConfig) o;

        if (sameOAuthUrl != that.sameOAuthUrl) return false;
        if (noVersionSuffix != that.noVersionSuffix) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (apiUrlFormat != null ? !apiUrlFormat.equals(that.apiUrlFormat) : that.apiUrlFormat != null)
            return false;
        if (credentialsType != null ? !credentialsType.equals(that.credentialsType) : that.credentialsType != null)
            return false;
        if (consumerKey != null ? !consumerKey.equals(that.consumerKey) : that.consumerKey != null)
            return false;
        return consumerSecret != null ? consumerSecret.equals(that.consumerSecret) : that.consumerSecret == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (apiUrlFormat != null ? apiUrlFormat.hashCode() : 0);
        result = 31 * result + (credentialsType != null ? credentialsType.hashCode() : 0);
        result = 31 * result + (sameOAuthUrl ? 1 : 0);
        result = 31 * result + (noVersionSuffix ? 1 : 0);
        result = 31 * result + (consumerKey != null ? consumerKey.hashCode() : 0);
        result = 31 * result + (consumerSecret != null ? consumerSecret.hashCode() : 0);
        return result;
    }

    public static final Creator<CustomAPIConfig> CREATOR = new Creator<CustomAPIConfig>() {
        public CustomAPIConfig createFromParcel(Parcel source) {
            CustomAPIConfig target = new CustomAPIConfig();
            CustomAPIConfigParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public CustomAPIConfig[] newArray(int size) {
            return new CustomAPIConfig[size];
        }
    };

    @NonNull
    public static List<CustomAPIConfig> listDefault(@NonNull Context context) {
        final AssetManager assets = context.getAssets();
        InputStream is = null;
        try {
            is = assets.open("data/default_api_configs.json");
            return JsonSerializer.parseList(is, CustomAPIConfig.class);
        } catch (IOException e) {
            return listBuiltin(context);
        } finally {
            RestFuUtils.closeSilently(is);
        }
    }

    public static CustomAPIConfig builtin(@NonNull Context context) {
        return new CustomAPIConfig(context.getString(R.string.provider_default), AccountType.TWITTER,
                DEFAULT_TWITTER_API_URL_FORMAT, Credentials.Type.OAUTH, true, false,
                TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
    }

    public static CustomAPIConfig mastodon(@NonNull Context context) {
        return new CustomAPIConfig(context.getString(R.string.provider_mastodon), AccountType.MASTODON,
                null, Credentials.Type.OAUTH2, true, true, null, null);
    }

    public static List<CustomAPIConfig> listBuiltin(@NonNull Context context) {
        return Collections.singletonList(builtin(context));
    }
}