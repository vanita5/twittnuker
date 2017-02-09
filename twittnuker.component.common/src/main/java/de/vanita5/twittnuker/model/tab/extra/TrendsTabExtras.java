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

package de.vanita5.twittnuker.model.tab.extra;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;

import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_WOEID;


@JsonObject
@ParcelablePlease
public class TrendsTabExtras extends TabExtras implements Parcelable {
    @JsonField(name = "woeid")
    int woeId;
    @JsonField(name = "place_name")
    String placeName;

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(final String placeName) {
        this.placeName = placeName;
    }

    public int getWoeId() {
        return woeId;
    }

    public void setWoeId(final int woeId) {
        this.woeId = woeId;
    }

    @Override
    public void copyToBundle(final Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putInt(EXTRA_WOEID, woeId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        TrendsTabExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<TrendsTabExtras> CREATOR = new Creator<TrendsTabExtras>() {
        public TrendsTabExtras createFromParcel(Parcel source) {
            TrendsTabExtras target = new TrendsTabExtras();
            TrendsTabExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public TrendsTabExtras[] newArray(int size) {
            return new TrendsTabExtras[size];
        }
    };

}