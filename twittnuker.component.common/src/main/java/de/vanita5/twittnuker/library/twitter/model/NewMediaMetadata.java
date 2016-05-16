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

package de.vanita5.twittnuker.library.twitter.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

@ParcelablePlease
@JsonObject
public class NewMediaMetadata implements Parcelable {
    @JsonField(name = "media_id")
    String mediaId;
    @JsonField(name = "alt_text")
    AltText altText;

    NewMediaMetadata() {

    }

    public NewMediaMetadata(String mediaId, String altText) {
        this.mediaId = mediaId;
        this.altText = new AltText(altText);
    }

    @Override
    public String toString() {
        return "NewMediaMetadata{" +
                "mediaId='" + mediaId + '\'' +
                ", altText=" + altText +
                '}';
    }

    @ParcelablePlease
    @JsonObject
    public static class AltText implements Parcelable {
        @JsonField(name = "text")
        String text;

        AltText() {

        }

        public AltText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "AltText{" +
                    "text='" + text + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            NewMediaMetadata$AltTextParcelablePlease.writeToParcel(this, dest, flags);
        }

        public static final Creator<AltText> CREATOR = new Creator<AltText>() {
            public AltText createFromParcel(Parcel source) {
                AltText target = new AltText();
                NewMediaMetadata$AltTextParcelablePlease.readFromParcel(target, source);
                return target;
            }

            public AltText[] newArray(int size) {
                return new AltText[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        NewMediaMetadataParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<NewMediaMetadata> CREATOR = new Creator<NewMediaMetadata>() {
        public NewMediaMetadata createFromParcel(Parcel source) {
            NewMediaMetadata target = new NewMediaMetadata();
            NewMediaMetadataParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public NewMediaMetadata[] newArray(int size) {
            return new NewMediaMetadata[size];
        }
    };
}