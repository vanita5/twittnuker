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

package de.vanita5.twittnuker.model.tab.argument;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import de.vanita5.twittnuker.model.UserKey;

@ParcelablePlease
@JsonObject
public class UserArguments extends TabArguments implements Parcelable {
    @JsonField(name = "user_id")
    String userId;
    @JsonField(name = "user_key")
    UserKey userKey;

    public void setUserKey(UserKey userKey) {
        this.userKey = userKey;
    }

    @Override
    public void copyToBundle(@NonNull Bundle bundle) {
        super.copyToBundle(bundle);
        if (userKey == null) {
            bundle.putParcelable(EXTRA_USER_KEY, UserKey.valueOf(userId));
        } else {
            bundle.putParcelable(EXTRA_USER_KEY, userKey);
        }
    }

    @Override
    public String toString() {
        return "UserArguments{" +
                "userId='" + userId + '\'' +
                ", userKey=" + userKey +
                "} " + super.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UserArgumentsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UserArguments> CREATOR = new Creator<UserArguments>() {
        public UserArguments createFromParcel(Parcel source) {
            UserArguments target = new UserArguments();
            UserArgumentsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UserArguments[] newArray(int size) {
            return new UserArguments[size];
        }
    };
}