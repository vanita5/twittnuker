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
import android.support.annotation.NonNull;

import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

@ParcelablePlease
public class AccountId implements Comparable<AccountId>, Parcelable {

    @ParcelableThisPlease
    long id;
    @ParcelableThisPlease
    String host;

    public AccountId(long id, String host) {
        this.id = id;
        this.host = host;
    }

    public AccountId(ParcelableAccount account) {
        this.id = account.account_id;
        this.host = account.account_host;
    }

    AccountId() {

    }

    public long getId() {
        return id;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        if (host != null) return id + "@" + host;
        return String.valueOf(id);
    }

    public static long[] getIds(AccountId[] ids) {
        long[] result = new long[ids.length];
        for (int i = 0, idsLength = ids.length; i < idsLength; i++) {
            result[i] = ids[i].getId();
        }
        return result;
    }

    @Override
    public int compareTo(@NonNull AccountId another) {
        if (this.id == another.id) {
            if (this.host != null && another.host != null) {
                return this.host.compareTo(another.host);
            } else if (this.host != null) {
                return 1;
            } else if (another.host != null) {
                return -1;
            }
            return 0;
        }
        return (int) (this.id - another.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountId accountId = (AccountId) o;

        return id == accountId.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        AccountIdParcelablePlease.writeToParcel(this, dest, flags);
    }

    public static final Creator<AccountId> CREATOR = new Creator<AccountId>() {
        public AccountId createFromParcel(Parcel source) {
            AccountId target = new AccountId();
            AccountIdParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public AccountId[] newArray(int size) {
            return new AccountId[size];
        }
    };
}