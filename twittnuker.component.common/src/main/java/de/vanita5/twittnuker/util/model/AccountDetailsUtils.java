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

package de.vanita5.twittnuker.util.model;

import com.bluelinelabs.logansquare.LoganSquare;

import de.vanita5.twittnuker.annotation.AccountType;
import de.vanita5.twittnuker.model.account.AccountExtras;
import de.vanita5.twittnuker.model.account.StatusNetAccountExtras;
import de.vanita5.twittnuker.model.account.TwitterAccountExtras;
import de.vanita5.twittnuker.model.account.cred.BasicCredentials;
import de.vanita5.twittnuker.model.account.cred.Credentials;
import de.vanita5.twittnuker.model.account.cred.EmptyCredentials;
import de.vanita5.twittnuker.model.account.cred.OAuth2Credentials;
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials;

import java.io.IOException;

public class AccountDetailsUtils {
    public static Credentials parseCredentials(String json, @Credentials.Type String type) {
        try {
            switch (type) {
                case Credentials.Type.OAUTH:
                case Credentials.Type.XAUTH: {
                    return LoganSquare.parse(json, OAuthCredentials.class);
                }
                case Credentials.Type.BASIC: {
                    return LoganSquare.parse(json, BasicCredentials.class);
                }
                case Credentials.Type.EMPTY: {
                    return LoganSquare.parse(json, EmptyCredentials.class);
                }
                case Credentials.Type.OAUTH2: {
                    return LoganSquare.parse(json, OAuth2Credentials.class);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new UnsupportedOperationException(type);
    }

    public static AccountExtras parseAccountExtras(String json, @AccountType String type) {
        if (json == null) return null;
        try {
            switch (type) {
                case AccountType.TWITTER: {
                    return LoganSquare.parse(json, TwitterAccountExtras.class);
                }
                case AccountType.STATUSNET: {
                    return LoganSquare.parse(json, StatusNetAccountExtras.class);
                }
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
}