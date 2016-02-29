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

package de.vanita5.twittnuker.shortener.gist;

import org.mariotaku.restfu.ExceptionFactory;
import org.mariotaku.restfu.RestAPIFactory;
import org.mariotaku.restfu.RestRequest;
import org.mariotaku.restfu.http.Authorization;
import org.mariotaku.restfu.http.Endpoint;
import org.mariotaku.restfu.http.HttpRequest;
import org.mariotaku.restfu.http.HttpResponse;
import org.mariotaku.restfu.urlconnection.URLConnectionRestClient;

public class GithubFactory {

    public static Github getInstance(final String apiKey) {
        final RestAPIFactory<GithubException> factory = new RestAPIFactory<>();
        factory.setEndpoint(new Endpoint("https://api.github.com/"));
        factory.setHttpClient(new URLConnectionRestClient());
        factory.setAuthorization(new Authorization() {
            @Override
            public String getHeader(Endpoint endpoint, RestRequest restRequest) {
                return "token " + apiKey;
            }

            @Override
            public boolean hasAuthorization() {
                return apiKey != null;
            }
        });
        factory.setExceptionFactory(new GithubExceptionFactory());
        factory.setRestConverterFactory(new GithubConverterFactory());
        return factory.build(Github.class);
    }

    private static class GithubExceptionFactory implements ExceptionFactory<GithubException> {
        @Override
        public GithubException newException(Throwable throwable, HttpRequest httpRequest, HttpResponse httpResponse) {
            GithubException exception;
            if (throwable != null) {
                exception = new GithubException(throwable);
            } else {
                exception = new GithubException();
            }
            exception.setRequest(httpRequest);
            exception.setResponse(httpResponse);
            return exception;
        }
    }
}