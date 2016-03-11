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
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;
import com.hannesdorfmann.parcelableplease.annotation.ParcelablePlease;
import com.hannesdorfmann.parcelableplease.annotation.ParcelableThisPlease;

import org.apache.commons.lang3.StringUtils;

@JsonObject
@ParcelablePlease
public class AccountKey implements Comparable<AccountKey>, Parcelable {

    public static final Creator<AccountKey> CREATOR = new Creator<AccountKey>() {
        public AccountKey createFromParcel(Parcel source) {
            AccountKey target = new AccountKey();
            AccountKeyParcelablePlease.readFromParcel(target, source);
            return target;
        }

        public AccountKey[] newArray(int size) {
            return new AccountKey[size];
        }
    };

    @JsonField(name = "id")
    @ParcelableThisPlease
    long id;
    @JsonField(name = "host")
    @ParcelableThisPlease
    String host;

    public AccountKey(long id, String host) {
        this.id = id;
        this.host = host;
    }

    public AccountKey(ParcelableAccount account) {
        this.id = account.account_id;
        this.host = account.account_host;
    }

    AccountKey() {

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

    @Override
    public int compareTo(@NonNull AccountKey another) {
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

        AccountKey accountKey = (AccountKey) o;

        return id == accountKey.id;

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
        AccountKeyParcelablePlease.writeToParcel(this, dest, flags);
    }

    public boolean isAccount(long accountId, String accountHost) {
        return this.id == accountId;
    }

    @Nullable
    public static AccountKey valueOf(@Nullable String str) {
        if (str == null) return null;
        int idxOfAt = str.indexOf("@");
        try {
            if (idxOfAt != -1) {
                final String idStr = str.substring(0, idxOfAt);
                return new AccountKey(Long.parseLong(idStr),
                        str.substring(idxOfAt + 1, str.length()));

            } else {
                return new AccountKey(Long.parseLong(str), null);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    public static AccountKey[] arrayOf(@Nullable String str) {
        if (str == null) return null;
        String[] split = StringUtils.split(str, ",");
        AccountKey[] keys = new AccountKey[split.length];
        for (int i = 0, splitLength = split.length; i < splitLength; i++) {
            keys[i] = valueOf(split[i]);
            if (keys[i] == null) return null;
        }
        return keys;
    }

    public static long[] getIds(AccountKey[] ids) {
        long[] result = new long[ids.length];
        for (int i = 0, idsLength = ids.length; i < idsLength; i++) {
            result[i] = ids[i].getId();
        }
        return result;
    }
}