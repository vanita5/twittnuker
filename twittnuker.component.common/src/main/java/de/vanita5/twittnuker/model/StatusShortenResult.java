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

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

@ParcelablePlease
@JsonObject
public class StatusShortenResult implements Parcelable {

    @JsonField(name = "shortened")
    @ParcelableThisPlease
    public String shortened;

    @JsonField(name = "extra")
    @ParcelableThisPlease
    public String extra;

    @JsonField(name = "error_code")
    @ParcelableThisPlease
    public int error_code;

    @JsonField(name = "error_message")
    @ParcelableThisPlease
    public String error_message;

    public StatusShortenResult() {
    }

    public StatusShortenResult(final int errorCode, final String errorMessage) {
        if (errorCode == 0) throw new IllegalArgumentException("Error code must not be 0");
        shortened = null;
        error_code = errorCode;
        error_message = errorMessage;
    }

    public StatusShortenResult(final String shortened) {
        if (shortened == null)
            throw new IllegalArgumentException("Shortened text must not be null");
        this.shortened = shortened;
        error_code = 0;
        error_message = null;
    }

    @Override
    public String toString() {
        return "StatusShortenResult{" +
                "shortened='" + shortened + '\'' +
                ", extra='" + extra + '\'' +
                ", error_code=" + error_code +
                ", error_message='" + error_message + '\'' +
                '}';
    }

    public static StatusShortenResult error(final int errorCode, final String errorMessage) {
        return new StatusShortenResult(errorCode, errorMessage);
    }

    public static StatusShortenResult shortened(final String shortened) {
        return new StatusShortenResult(shortened);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        StatusShortenResultParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<StatusShortenResult> CREATOR = new Creator<StatusShortenResult>() {
        public StatusShortenResult createFromParcel(Parcel source) {
            StatusShortenResult target = new StatusShortenResult();
            StatusShortenResultParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public StatusShortenResult[] newArray(int size) {
            return new StatusShortenResult[size];
        }
    };
}