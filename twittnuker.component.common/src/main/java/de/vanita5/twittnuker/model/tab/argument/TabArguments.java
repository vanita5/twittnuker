/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.model.tab.argument;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.annotation.CustomTabType;
import de.vanita5.twittnuker.model.UserKey;

import java.io.IOException;
import java.util.Arrays;

@JsonObject
public class TabArguments implements TwittnukerConstants {
    @JsonField(name = "account_id")
    String accountId;

    @JsonField(name = "account_keys")
    @Nullable
    UserKey[] accountKeys;

    @Nullable
    public UserKey[] getAccountKeys() {
        return accountKeys;
    }

    public void setAccountKeys(@Nullable UserKey[] accountKeys) {
        this.accountKeys = accountKeys;
    }

    public String getAccountId() {
        return accountId;
    }

    @CallSuper
    public void copyToBundle(@NonNull Bundle bundle) {
        final UserKey[] accountKeys = this.accountKeys;
        if (accountKeys != null && accountKeys.length > 0) {
            for (UserKey key : accountKeys) {
                if (key == null) return;
            }
            bundle.putParcelableArray(EXTRA_ACCOUNT_KEYS, accountKeys);
        } else if (accountId != null) {
            long id = Long.MIN_VALUE;
            try {
                id = Long.parseLong(accountId);
            } catch (NumberFormatException e) {
                // Ignore
            }
            if (id != Long.MIN_VALUE && id <= 0) {
                // account_id = -1, means no account selected
                bundle.putParcelableArray(EXTRA_ACCOUNT_KEYS, null);
                return;
            }
            bundle.putParcelableArray(EXTRA_ACCOUNT_KEYS, new UserKey[]{UserKey.valueOf(accountId)});
        }
    }

    @Override
    public String toString() {
        return "TabArguments{" +
                "accountId=" + accountId +
                ", accountKeys=" + Arrays.toString(accountKeys) +
                '}';
    }

    @Nullable
    public static TabArguments parse(@NonNull @CustomTabType String type, @Nullable String json) throws IOException {
        if (json == null) return null;
        switch (type) {
            case CustomTabType.HOME_TIMELINE:
            case CustomTabType.NOTIFICATIONS_TIMELINE:
            case CustomTabType.DIRECT_MESSAGES: {
                return LoganSquare.parse(json, TabArguments.class);
            }
            case CustomTabType.USER_TIMELINE:
            case CustomTabType.FAVORITES: {
                return LoganSquare.parse(json, UserArguments.class);
            }
            case CustomTabType.LIST_TIMELINE: {
                return LoganSquare.parse(json, UserListArguments.class);
            }
            case CustomTabType.SEARCH_STATUSES: {
                return LoganSquare.parse(json, TextQueryArguments.class);
            }
        }
        return null;
    }
}