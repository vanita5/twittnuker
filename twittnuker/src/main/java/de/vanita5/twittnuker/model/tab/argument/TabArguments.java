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

package de.vanita5.twittnuker.model.tab.argument;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import de.vanita5.twittnuker.constant.IntentConstants;
import de.vanita5.twittnuker.model.UserKey;

import java.util.Arrays;

@JsonObject
public class TabArguments implements IntentConstants {
    @JsonField(name = "account_id")
    long accountId = -1;

    @JsonField(name = "account_keys")
    UserKey[] accountKeys;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public UserKey[] getAccountKeys() {
        return accountKeys;
    }

    public void setAccountKeys(UserKey[] accountKeys) {
        this.accountKeys = accountKeys;
    }

    @CallSuper
    public void copyToBundle(@NonNull Bundle bundle) {
        if (accountId > 0) {
            bundle.putLong(EXTRA_ACCOUNT_ID, accountId);
        }
    }

    @Override
    public String toString() {
        return "TabArguments{" +
                "accountId=" + accountId +
                ", accountKeys=" + Arrays.toString(accountKeys) +
                '}';
    }
}