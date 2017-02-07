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

package de.vanita5.twittnuker.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.mariotaku.library.objectcursor.annotation.CursorField;
import org.mariotaku.library.objectcursor.annotation.CursorObject;
import de.vanita5.twittnuker.model.util.UserKeyConverter;
import de.vanita5.twittnuker.model.util.UserKeyCursorFieldConverter;
import de.vanita5.twittnuker.provider.TwidereDataStore;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends;


@CursorObject(valuesCreator = true, tableInfo = true)
@JsonObject
@ParcelablePlease
public class ParcelableTrend implements Parcelable {

    @CursorField(value = CachedTrends._ID, excludeWrite = true, type = TwidereDataStore.TYPE_PRIMARY_KEY)
    long _id;
    @SuppressWarnings("NullableProblems")
    @ParcelableThisPlease
    @JsonField(name = "account_id", typeConverter = UserKeyConverter.class)
    @CursorField(value = CachedTrends.ACCOUNT_KEY, converter = UserKeyCursorFieldConverter.class)
    public UserKey account_key;
    @ParcelableThisPlease
    @JsonField(name = "woe_id")
    @CursorField(CachedTrends.WOEID)
    public int woe_id;
    @ParcelableThisPlease
    @JsonField(name = "timestamp")
    @CursorField(CachedTrends.TIMESTAMP)
    public long timestamp;
    @JsonField(name = "name")
    @CursorField(value = CachedTrends.NAME)
    public String name;

    @Override
    public String toString() {
        return "ParcelableTrend{" +
                "_id=" + _id +
                ", account_key=" + account_key +
                ", woe_id=" + woe_id +
                ", timestamp=" + timestamp +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelableTrendParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<ParcelableTrend> CREATOR = new Creator<ParcelableTrend>() {
        public ParcelableTrend createFromParcel(Parcel source) {
            ParcelableTrend target = new ParcelableTrend();
            ParcelableTrendParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public ParcelableTrend[] newArray(int size) {
            return new ParcelableTrend[size];
        }
    };
}