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

package de.vanita5.twittnuker.api.twitter.api;

import org.mariotaku.restfu.annotation.method.GET;

import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.api.twitter.model.Language;
import de.vanita5.twittnuker.api.twitter.model.RateLimitStatus;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.TwitterAPIConfiguration;

import java.util.Map;

@SuppressWarnings("RedundantThrows")
public interface HelpResources {
    /**
     * Returns the current configuration used by Twitter including twitter.com
     * slugs which are not usernames, maximum photo resolutions, and t.co URL
     * lengths.</br> It is recommended applications request this endpoint when
     * they are loaded, but no more than once a day.
     *
     * @return configuration
     * @throws TwitterException when Twitter service or network is
     *                          unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/help/configuration">GET
     * help/configuration | Twitter Developers</a>
     * @since Twitter4J 2.2.3
     */
    @GET("/help/configuration.json")
    TwitterAPIConfiguration getAPIConfiguration() throws TwitterException;

    /**
     * Returns the list of languages supported by Twitter along with their ISO
     * 639-1 code. The ISO 639-1 code is the two letter value to use if you
     * include lang with any of your requests.
     *
     * @return list of languages supported by Twitter
     * @throws TwitterException when Twitter service or network is
     *                          unavailable
     * @see <a
     * href="https://dev.twitter.com/docs/api/1.1/get/help/languages">GET
     * help/languages | Twitter Developers</a>
     * @since Twitter4J 2.2.3
     */
    @GET("/help/languages.json")
    ResponseList<Language> getLanguages() throws TwitterException;

    String getPrivacyPolicy() throws TwitterException;

    Map<String, RateLimitStatus> getRateLimitStatus() throws TwitterException;

    Map<String, RateLimitStatus> getRateLimitStatus(String... resources) throws TwitterException;

    String getTermsOfService() throws TwitterException;

}