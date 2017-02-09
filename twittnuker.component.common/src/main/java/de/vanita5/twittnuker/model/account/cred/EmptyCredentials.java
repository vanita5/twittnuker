/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.model.account.cred;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;


@ParcelablePlease
@JsonObject
public class EmptyCredentials extends Credentials implements Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        EmptyCredentialsParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<EmptyCredentials> CREATOR = new Creator<EmptyCredentials>() {
        public EmptyCredentials createFromParcel(Parcel source) {
            EmptyCredentials target = new EmptyCredentials();
            EmptyCredentialsParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public EmptyCredentials[] newArray(int size) {
            return new EmptyCredentials[size];
        }
    };
}
