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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@JsonObject
@ParcelablePlease
public class ParcelableMediaUpdate implements Parcelable {

    @SuppressWarnings("NullableProblems")
    @NonNull
    @JsonField(name = "uri")
    public String uri;
    @ParcelableMedia.Type
    @JsonField(name = "type")
    public int type;

    @JsonField(name = "alt_text")
    @Nullable
    public String alt_text;

    public ParcelableMediaUpdate() {
    }

    public ParcelableMediaUpdate(@NonNull final String uri, final int type) {
        this.uri = uri;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParcelableMediaUpdate that = (ParcelableMediaUpdate) o;

        if (type != that.type) return false;
        if (!uri.equals(that.uri)) return false;
        return alt_text != null ? alt_text.equals(that.alt_text) : that.alt_text == null;

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + type;
        result = 31 * result + (alt_text != null ? alt_text.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParcelableMediaUpdate{" +
                "uri='" + uri + '\'' +
                ", type=" + type +
                ", alt_text='" + alt_text + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableMediaUpdateParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableMediaUpdate> CREATOR = new Creator<ParcelableMediaUpdate>() {
        @Override
        public ParcelableMediaUpdate createFromParcel(Parcel source) {
            ParcelableMediaUpdate target = new ParcelableMediaUpdate();
            ParcelableMediaUpdateParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public ParcelableMediaUpdate[] newArray(int size) {
            return new ParcelableMediaUpdate[size];
        }
    };
}