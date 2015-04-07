/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package twitter4j.media;

import twitter4j.TwitterException;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpParameter;

/**
 * @author Takao Nakaguchi - takao.nakaguchi at gmail.com
 * @author withgod - noname at withgod.jp
 * @since Twitter4J 2.1.8
 */
class TwitgooUpload extends AbstractImageUploadImpl {

    public TwitgooUpload(Configuration conf, OAuthAuthorization oauth) {
        super(conf, oauth);
    }


    @Override
    protected String postUpload() throws TwitterException {
        int statusCode = httpResponse.getStatusCode();
        if (statusCode != 200)
            throw new TwitterException("Twitgoo image upload returned invalid status code", httpResponse);

        String response = httpResponse.asString();
        if (response.contains("<rsp status=\"ok\">")) {
            String h = "<mediaurl>";
            int i = response.indexOf(h);
            if (i != -1) {
                int j = response.indexOf("</mediaurl>", i + h.length());
                if (j != -1) {
                    return response.substring(i + h.length(), j);
                }
            }
        } else if (response.contains("<rsp status=\"fail\">")) {
            String h = "msg=\"";
            int i = response.indexOf(h);
            if (i != -1) {
                int j = response.indexOf("\"", i + h.length());
                if (j != -1) {
                    String msg = response.substring(i + h.length(), j);
                    throw new TwitterException("Invalid Twitgoo response: " + msg);
                }
            }
        }

        throw new TwitterException("Unknown Twitgoo response", httpResponse);
    }

    @Override
    protected void preUpload() throws TwitterException {
        uploadUrl = "http://twitgoo.com/api/uploadAndPost";
        String verifyCredentialsAuthorizationHeader = generateVerifyCredentialsAuthorizationHeader(TWITTER_VERIFY_CREDENTIALS_JSON_V1_1);

        headers.addHeader("X-Auth-Service-Provider", TWITTER_VERIFY_CREDENTIALS_JSON_V1_1);
        headers.addHeader("X-Verify-Credentials-Authorization", verifyCredentialsAuthorizationHeader);

        HttpParameter[] params = {
                new HttpParameter("no_twitter_post", "1"),
                this.image
        };
        if (message != null) {
            params = appendHttpParameters(new HttpParameter[]{
                    this.message
            }, params);
        }
        this.postParameter = params;
    }
}
