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

package de.vanita5.twittnuker.model.tab.extra;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import de.vanita5.twittnuker.constant.IntentConstants;

@ParcelablePlease
@JsonObject
public class InteractionsTabExtras extends TabExtras implements Parcelable {

    @ParcelableThisPlease
    @JsonField(name = "my_following_only")
    boolean myFollowingOnly;

    @ParcelableThisPlease
    @JsonField(name = "mentions_only")
    boolean mentionsOnly;

    public boolean isMyFollowingOnly() {
        return myFollowingOnly;
    }

    public void setMyFollowingOnly(boolean myFollowingOnly) {
        this.myFollowingOnly = myFollowingOnly;
    }

    public boolean isMentionsOnly() {
        return mentionsOnly;
    }

    public void setMentionsOnly(boolean mentionsOnly) {
        this.mentionsOnly = mentionsOnly;
    }

    @Override
    public void copyToBundle(Bundle bundle) {
        super.copyToBundle(bundle);
        bundle.putBoolean(IntentConstants.EXTRA_MY_FOLLOWING_ONLY, myFollowingOnly);
        bundle.putBoolean(IntentConstants.EXTRA_MENTIONS_ONLY, mentionsOnly);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        InteractionsTabExtrasParcelablePlease.writeToParcel(this, dest, flags);
    }

    @Override
    public String toString() {
        return "InteractionsTabExtras{" +
                "myFollowingOnly=" + myFollowingOnly +
                ", mentionsOnly=" + mentionsOnly +
                "} " + super.toString();
    }

    public static final Creator<InteractionsTabExtras> CREATOR = new Creator<InteractionsTabExtras>() {
        @Override
        public InteractionsTabExtras createFromParcel(Parcel source) {
            InteractionsTabExtras target = new InteractionsTabExtras();
            InteractionsTabExtrasParcelablePlease.readFromParcel(target, source);
            return target;
        }

        @Override
        public InteractionsTabExtras[] newArray(int size) {
            return new InteractionsTabExtras[size];
        }
    };
}