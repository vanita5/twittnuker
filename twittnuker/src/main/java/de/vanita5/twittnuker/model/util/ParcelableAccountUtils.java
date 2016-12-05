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

package de.vanita5.twittnuker.model.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.annotation.AccountType;
import de.vanita5.twittnuker.extension.AccountExtensionsKt;
import de.vanita5.twittnuker.model.ParcelableAccount;
import de.vanita5.twittnuker.model.ParcelableAccountExtensionsKt;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.account.cred.Credentials;
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials;
import de.vanita5.twittnuker.util.TwitterContentUtils;

import java.util.ArrayList;

public class ParcelableAccountUtils {

    private ParcelableAccountUtils() {
    }

    public static UserKey[] getAccountKeys(@NonNull ParcelableAccount[] accounts) {
        UserKey[] ids = new UserKey[accounts.length];
        for (int i = 0, j = accounts.length; i < j; i++) {
            ids[i] = accounts[i].account_key;
        }
        return ids;
    }

    @Nullable
    public static ParcelableAccount getAccount(@NonNull final Context context,
                                               @NonNull final UserKey accountKey) {
        final AccountManager am = AccountManager.get(context);
        final Account account = AccountUtils.findByAccountKey(am, accountKey);
        if (account == null) return null;
        return ParcelableAccountExtensionsKt.toParcelableAccount(account, am);
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(final Context context, final boolean activatedOnly,
                                                  final boolean officialKeyOnly) {
        ArrayList<Account> accounts = new ArrayList<>();
        final AccountManager am = AccountManager.get(context);
        for (Account account : AccountUtils.getAccounts(am)) {
            boolean activated = AccountExtensionsKt.isAccountActivated(account, am);
            if (!activated && activatedOnly) continue;
            boolean isOfficialKey = isOfficialKey(context, account, am);
            if (!isOfficialKey && officialKeyOnly) continue;
            accounts.add(account);
        }
        return getAccounts(am, accounts.toArray(new Account[accounts.size()]));
    }

    static boolean isOfficialKey(Context context, Account account, AccountManager am) {
        final String credentialsType = AccountExtensionsKt.getCredentialsType(account, am);
        if (!Credentials.Type.OAUTH.equals(credentialsType) && !Credentials.Type.XAUTH.equals(credentialsType)) {
            return false;
        }
        final OAuthCredentials credentials = (OAuthCredentials) AccountExtensionsKt.getCredentials(account, am);
        return TwitterContentUtils.isOfficialKey(context, credentials.consumer_key, credentials.consumer_secret);
    }

    public static ParcelableAccount[] getAccounts(@NonNull final Context context) {
        final AccountManager am = AccountManager.get(context);
        return getAccounts(am, AccountUtils.getAccounts(am));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@NonNull final Context context, @NonNull final UserKey... accountIds) {
        ArrayList<Account> accounts = new ArrayList<>();
        final AccountManager am = AccountManager.get(context);
        for (Account account : AccountUtils.getAccounts(am)) {
            if (ArrayUtils.contains(accountIds, AccountExtensionsKt.getAccountKey(account, am))) {
                accounts.add(account);
            }
        }
        return getAccounts(am, accounts.toArray(new Account[accounts.size()]));
    }

    @NonNull
    public static ParcelableAccount[] getAccounts(@NonNull final AccountManager am, @Nullable final Account[] accounts) {
        if (accounts == null) return new ParcelableAccount[0];
        final ParcelableAccount[] parcelableAccounts = new ParcelableAccount[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            parcelableAccounts[i] = ParcelableAccountExtensionsKt.toParcelableAccount(accounts[i], am);
        }
        return parcelableAccounts;
    }

    @NonNull
    @AccountType
    public static String getAccountType(@NonNull ParcelableAccount account) {
        if (account.account_type == null) return AccountType.TWITTER;
        return account.account_type;
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
}