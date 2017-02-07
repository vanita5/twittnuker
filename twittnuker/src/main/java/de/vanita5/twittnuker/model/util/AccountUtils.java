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

package de.vanita5.twittnuker.model.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.annotation.AccountType;
import de.vanita5.twittnuker.annotation.AuthTypeInt;
import de.vanita5.twittnuker.extension.model.AccountDetailsExtensionsKt;
import de.vanita5.twittnuker.extension.model.AccountExtensionsKt;
import de.vanita5.twittnuker.model.AccountDetails;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.account.cred.Credentials;

import java.util.Arrays;

import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_TYPE;
import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_ACTIVATED;
import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_COLOR;
import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_CREDS_TYPE;
import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_EXTRAS;
import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_KEY;
import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_POSITION;
import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_TYPE;
import static de.vanita5.twittnuker.TwittnukerConstants.ACCOUNT_USER_DATA_USER;

public class AccountUtils {

    public static final String[] ACCOUNT_USER_DATA_KEYS = {
            ACCOUNT_USER_DATA_KEY,
            ACCOUNT_USER_DATA_TYPE,
            ACCOUNT_USER_DATA_CREDS_TYPE,
            ACCOUNT_USER_DATA_ACTIVATED,
            ACCOUNT_USER_DATA_USER,
            ACCOUNT_USER_DATA_EXTRAS,
            ACCOUNT_USER_DATA_COLOR,
            ACCOUNT_USER_DATA_POSITION,
    };

    @Nullable
    public static Account findByAccountKey(@NonNull AccountManager am, @NonNull UserKey userKey) {
        for (Account account : getAccounts(am)) {
            if (userKey.equals(AccountExtensionsKt.getAccountKey(account, am))) {
                return account;
            }
        }
        return null;
    }

    public static Account[] getAccounts(@NonNull AccountManager am) {
        //noinspection MissingPermission
        return am.getAccountsByType(ACCOUNT_TYPE);
    }

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am, @NonNull Account[] accounts, boolean getCredentials) {
        AccountDetails[] details = new AccountDetails[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            details[i] = getAccountDetails(am, accounts[i], getCredentials);
        }
        Arrays.sort(details);
        return details;
    }

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am, @NonNull UserKey[] accountKeys, boolean getCredentials) {
        AccountDetails[] details = new AccountDetails[accountKeys.length];
        for (int i = 0; i < accountKeys.length; i++) {
            details[i] = getAccountDetails(am, accountKeys[i], getCredentials);
        }
        Arrays.sort(details);
        return details;
    }

    public static AccountDetails[] getAllAccountDetails(@NonNull AccountManager am, boolean getCredentials) {
        return getAllAccountDetails(am, getAccounts(am), getCredentials);
    }

    @Nullable
    public static AccountDetails getAccountDetails(@NonNull AccountManager am, @NonNull UserKey accountKey, boolean getCredentials) {
        final Account account = findByAccountKey(am, accountKey);
        if (account == null) return null;
        return getAccountDetails(am, account, getCredentials);
    }

    public static AccountDetails getAccountDetails(@NonNull AccountManager am, @NonNull Account account, boolean getCredentials) {
        AccountDetails details = new AccountDetails();
        details.key = AccountExtensionsKt.getAccountKey(account, am);
        details.account = account;
        details.color = AccountExtensionsKt.getColor(account, am);
        details.position = AccountExtensionsKt.getPosition(account, am);
        details.activated = AccountExtensionsKt.isActivated(account, am);
        details.type = AccountExtensionsKt.getAccountType(account, am);
        details.credentials_type = AccountExtensionsKt.getCredentialsType(account, am);
        details.user = AccountExtensionsKt.getAccountUser(account, am);
        details.user.color = details.color;

        details.extras = AccountExtensionsKt.getAccountExtras(account, am);

        if (getCredentials) {
            details.credentials = AccountExtensionsKt.getCredentials(account, am);
        }
        return details;
    }

    @Nullable
    public static Account findByScreenName(AccountManager am, @NonNull String screenName) {
        for (Account account : getAccounts(am)) {
            if (screenName.equalsIgnoreCase(AccountExtensionsKt.getAccountUser(account, am).screen_name)) {
                return account;
            }
        }
        return null;
    }

    public static boolean hasOfficialKeyAccount(Context context) {
        for (AccountDetails details : getAllAccountDetails(AccountManager.get(context), true)) {
            if (AccountDetailsExtensionsKt.isOfficial(details, context)) {
                return true;
            }
        }
        return false;
    }

    public static int getAccountTypeIcon(@Nullable String accountType) {
        if (accountType == null) return R.drawable.ic_account_logo_twitter;
        switch (accountType) {
            case AccountType.TWITTER: {
                return R.drawable.ic_account_logo_twitter;
            }
            case AccountType.FANFOU: {
                return R.drawable.ic_account_logo_fanfou;
            }
            case AccountType.STATUSNET: {
                return R.drawable.ic_account_logo_statusnet;
            }

        }
        return R.drawable.ic_account_logo_twitter;
    }

    public static String getCredentialsType(@AuthTypeInt int authType) {
        switch (authType) {
            case AuthTypeInt.OAUTH:
                return Credentials.Type.OAUTH;
            case AuthTypeInt.BASIC:
                return Credentials.Type.BASIC;
            case AuthTypeInt.TWIP_O_MODE:
                return Credentials.Type.EMPTY;
            case AuthTypeInt.XAUTH:
                return Credentials.Type.XAUTH;
            case AuthTypeInt.OAUTH2:
                return Credentials.Type.OAUTH2;
        }
        throw new UnsupportedOperationException();
    }


    public static boolean hasAccountPermission(@NonNull AccountManager am) {
        try {
            getAccounts(am);
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }



}