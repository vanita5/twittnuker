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

package de.vanita5.twittnuker.model.draft;

import android.os.Parcel;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

@ParcelablePlease
@JsonObject
public class SendDirectMessageActionExtras implements ActionExtras {
    @ParcelableThisPlease
    @JsonField(name = "recipient_id")
    String recipientId;

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SendDirectMessageActionExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SendDirectMessageActionExtras that = (SendDirectMessageActionExtras) o;

        return recipientId != null ? recipientId.equals(that.recipientId) : that.recipientId == null;

    }

    @Override
    public int hashCode() {
        return recipientId != null ? recipientId.hashCode() : 0;
    }

    public static final Creator<SendDirectMessageActionExtras> CREATOR = new Creator<SendDirectMessageActionExtras>() {
        @Override
        public SendDirectMessageActionExtras createFromParcel(Parcel source) {
            SendDirectMessageActionExtras target = new SendDirectMessageActionExtras();
            SendDirectMessageActionExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public SendDirectMessageActionExtras[] newArray(int size) {
            return new SendDirectMessageActionExtras[size];
        }
    };
}