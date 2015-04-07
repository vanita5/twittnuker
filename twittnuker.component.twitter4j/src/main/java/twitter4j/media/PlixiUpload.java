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
class PlixiUpload extends AbstractImageUploadImpl {
// Described at http://groups.google.com/group/tweetphoto/web/multipart-form-data-upload
//  and http://groups.google.com/group/tweetphoto/web/oauth-echo

    public PlixiUpload(Configuration conf, String apiKey, OAuthAuthorization oauth) {
        super(conf, apiKey, oauth);
        logger.warn("Lockerz is no longer providing API.");
        this.uploadUrl = "http://api.plixi.com/api/upload.aspx";//"https://api.plixi.com/api/tpapi.svc/upload2";
    }

    @Override
    protected String postUpload() throws TwitterException {
        int statusCode = httpResponse.getStatusCode();
        if (statusCode != 201)
            throw new TwitterException("Plixi image upload returned invalid status code", httpResponse);

        String response = httpResponse.asString();

        if (response.contains("<Error><ErrorCode>")) {
            String error = response.substring(response.indexOf("<ErrorCode>") + "<ErrorCode>".length(), response.lastIndexOf("</ErrorCode>"));
            throw new TwitterException("Plixi image upload failed with this error message: " + error, httpResponse);
        }
        if (response.contains("<Status>OK</Status>")) {
            return response.substring(response.indexOf("<MediaUrl>") + "<MediaUrl>".length(), response.indexOf("</MediaUrl>"));
        }

        throw new TwitterException("Unknown Plixi response", httpResponse);
    }

    @Override
    protected void preUpload() throws TwitterException {
        String verifyCredentialsAuthorizationHeader = generateVerifyCredentialsAuthorizationHeader(TWITTER_VERIFY_CREDENTIALS_JSON_V1_1);

        headers.addHeader("X-Auth-Service-Provider", TWITTER_VERIFY_CREDENTIALS_JSON_V1_1);
        headers.addHeader("X-Verify-Credentials-Authorization", verifyCredentialsAuthorizationHeader);

        if (null == apiKey) {
            throw new IllegalStateException("No API Key for Plixi specified. put media.providerAPIKey in twitter4j.properties.");
        }
        HttpParameter[] params = {
                new HttpParameter("api_key", apiKey),
                this.image
        };
        if (message != null) {
            params = appendHttpParameters(new HttpParameter[]{
                    this.message}, params);
        }
        this.postParameter = params;
    }
}
