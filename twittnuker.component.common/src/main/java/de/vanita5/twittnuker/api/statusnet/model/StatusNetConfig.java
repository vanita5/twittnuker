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

package de.vanita5.twittnuker.api.statusnet.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
@JsonObject
public class StatusNetConfig implements Parcelable {

    @JsonField(name = "site")
    Site site;

    public Site getSite() {
        return site;
    }

    @ParcelablePlease
    @JsonObject
    public static class Site implements Parcelable {
        @JsonField(name = "textlimit")
        int textLimit;

        public int getTextLimit() {
            return textLimit;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            StatusNetConfig$SiteParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<Site> CREATOR = new Creator<Site>() {
            @Override
            public Site createFromParcel(Parcel source) {
                Site target = new Site();
                StatusNetConfig$SiteParcelablePlease.readFromParcel(target, source);
                return target;
            }

            @Override
            public Site[] newArray(int size) {
                return new Site[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusNetConfigParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusNetConfig> CREATOR = new Creator<StatusNetConfig>() {
        @Override
        public StatusNetConfig createFromParcel(Parcel source) {
            StatusNetConfig target = new StatusNetConfig();
            StatusNetConfigParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public StatusNetConfig[] newArray(int size) {
            return new StatusNetConfig[size];
        }
    };
}