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

package de.vanita5.twittnuker.library.twitter.auth;

import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;

public class BearerAuthorization implements Authorization {
    private final String accessToken;

    public BearerAuthorization(String accessToken) {
        this.accessToken = accessToken;
    }


    @Override
    public String getHeader(Endpoint endpoint, RestRequest info) {
        return "Bearer " + accessToken;
    }

    @Override
    public boolean hasAuthorization() {
        return accessToken != null;
    }
}