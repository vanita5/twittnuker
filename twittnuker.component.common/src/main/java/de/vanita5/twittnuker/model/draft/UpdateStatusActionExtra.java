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

package de.vanita5.twittnuker.model.draft;

import android.os.Parcel;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import de.vanita5.twittnuker.model.ParcelableStatus;

@ParcelablePlease
@JsonObject
public class UpdateStatusActionExtra implements ActionExtra {
    @ParcelableThisPlease
    @JsonField(name = "in_reply_to_status")
    ParcelableStatus inReplyToStatus;
    @ParcelableThisPlease
    @JsonField(name = "is_possibly_sensitive")
    boolean isPossiblySensitive;
    @ParcelableThisPlease
    @JsonField(name = "repost_status_id")
    String repostStatusId;
    @ParcelableThisPlease
    @JsonField(name = "display_coordinates")
    boolean displayCoordinates;

    public ParcelableStatus getInReplyToStatus() {
        return inReplyToStatus;
    }

    public void setInReplyToStatus(ParcelableStatus inReplyToStatus) {
        this.inReplyToStatus = inReplyToStatus;
    }

    public boolean isPossiblySensitive() {
        return isPossiblySensitive;
    }

    public void setIsPossiblySensitive(boolean isPossiblySensitive) {
        this.isPossiblySensitive = isPossiblySensitive;
    }

    public String isRepostStatusId() {
        return repostStatusId;
    }

    public void setRepostStatusId(String repostStatusId) {
        this.repostStatusId = repostStatusId;
    }

    public boolean getDisplayCoordinates() {
        return displayCoordinates;
    }

    public void setDisplayCoordinates(boolean displayCoordinates) {
        this.displayCoordinates = displayCoordinates;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        UpdateStatusActionExtraParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<UpdateStatusActionExtra> CREATOR = new Creator<UpdateStatusActionExtra>() {
        public UpdateStatusActionExtra createFromParcel(Parcel source) {
            UpdateStatusActionExtra target = new UpdateStatusActionExtra();
            UpdateStatusActionExtraParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public UpdateStatusActionExtra[] newArray(int size) {
            return new UpdateStatusActionExtra[size];
        }
    };
}