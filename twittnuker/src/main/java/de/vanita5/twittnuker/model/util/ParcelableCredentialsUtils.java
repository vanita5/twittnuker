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

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.annotation.AuthTypeInt;
import de.vanita5.twittnuker.model.ParcelableCredentialsExtensionsKt;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.account.cred.Credentials;
import de.vanita5.twittnuker.util.TwitterContentUtils;

import java.util.ArrayList;
import java.util.List;

public class ParcelableCredentialsUtils {
    private ParcelableCredentialsUtils() {
    }

    public static boolean isOAuth(int authType) {
        switch (authType) {
            case AuthTypeInt.OAUTH:
            case AuthTypeInt.XAUTH: {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ParcelableCredentials getCredentials(@NonNull final Context context,
                                                       @NonNull final UserKey accountKey) {
        final AccountManager am = AccountManager.get(context);
        final Account account = AccountUtils.findByAccountKey(am, accountKey);
        if (account == null) return null;
        return ParcelableCredentialsExtensionsKt.toParcelableCredentials(account, am);
    }

    @NonNull
    public static List<ParcelableCredentials> getCredentialses(final Context context, final boolean activatedOnly,
                                                               final boolean officialKeyOnly) {
        ArrayList<ParcelableCredentials> credentialses = new ArrayList<>();
        for (ParcelableCredentials credentials : getCredentialses(context)) {
            boolean activated = credentials.is_activated;
            if (!activated && activatedOnly) continue;
            boolean isOfficialKey = TwitterContentUtils.isOfficialKey(context,
                    credentials.consumer_key, credentials.consumer_secret);
            if (!isOfficialKey && officialKeyOnly) continue;
            credentialses.add(credentials);
        }
        return credentialses;
    }


    public static ParcelableCredentials[] getCredentialses(@NonNull final Context context) {
        final AccountManager am = AccountManager.get(context);
        final Account[] accounts = AccountUtils.getAccounts(am);
        final ParcelableCredentials[] credentialses = new ParcelableCredentials[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            credentialses[i] = ParcelableCredentialsExtensionsKt.toParcelableCredentials(accounts[i], am);
        }
        return credentialses;
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
}
