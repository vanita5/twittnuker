/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.model.account;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

@ParcelablePlease
@JsonObject
public class TwitterAccountExtras implements Parcelable, AccountExtras {

    public static final Creator<TwitterAccountExtras> CREATOR = new Creator<TwitterAccountExtras>() {
        @Override
        public TwitterAccountExtras createFromParcel(Parcel source) {
            TwitterAccountExtras target = new TwitterAccountExtras();
            TwitterAccountExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public TwitterAccountExtras[] newArray(int size) {
            return new TwitterAccountExtras[size];
        }
    };

    @JsonField(name = "official_credentials")
    @ParcelableThisPlease
    boolean officialCredentials;

    public boolean isOfficialCredentials() {
        return officialCredentials;
    }

    public void setIsOfficialCredentials(boolean officialCredentials) {
        this.officialCredentials = officialCredentials;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TwitterAccountExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }
}