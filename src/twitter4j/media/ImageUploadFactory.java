/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import twitter4j.auth.Authorization;
import twitter4j.auth.AuthorizationFactory;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationContext;

import static twitter4j.media.MediaProvider.*;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.12
 */
public class ImageUploadFactory {
    private final Configuration conf;
    private final MediaProvider defaultMediaProvider;
    private final String apiKey;

    /**
     * Creates an ImageUploadFactory with default configuration
     */
    public ImageUploadFactory() {
        this(ConfigurationContext.getInstance());
    }

    /**
     * Creates an ImageUploadFactory with the specified configuration
     */
    public ImageUploadFactory(Configuration conf) {
        String mediaProvider = conf.getMediaProvider().toLowerCase();
        if ("imgly".equals(mediaProvider) || "img_ly".equals(mediaProvider)) {
            defaultMediaProvider = IMG_LY;
        } else if ("plixi".equals(mediaProvider)) {
            defaultMediaProvider = PLIXI;
        } else if ("lockerz".equals(mediaProvider)) {
            defaultMediaProvider = LOCKERZ;
        } else if ("twipple".equals(mediaProvider)) {
            defaultMediaProvider = TWIPPLE;
        } else if ("twitgoo".equals(mediaProvider)) {
            defaultMediaProvider = TWITGOO;
        } else if ("yfrog".equals(mediaProvider)) {
            defaultMediaProvider = YFROG;
        } else if ("mobypicture".equals(mediaProvider)) {
            defaultMediaProvider = MOBYPICTURE;
        } else if ("posterous".equals(mediaProvider)) {
            defaultMediaProvider = POSTEROUS;
        } else {
            throw new IllegalArgumentException("unsupported media provider:" + mediaProvider);
        }
        this.conf = conf;
        apiKey = conf.getMediaProviderAPIKey();
    }

    /**
     * Returns an ImageUpload instance associated with the default media provider
     *
     * @return ImageUpload
     */
    public ImageUpload getInstance() {
        return getInstance(defaultMediaProvider);
    }

    /**
     * Returns an ImageUpload instance associated with the default media provider
     *
     * @param authorization authorization
     * @return ImageUpload
     * @since Twitter4J 2.1.11
     */
    public ImageUpload getInstance(Authorization authorization) {
        return getInstance(defaultMediaProvider, authorization);
    }

    /**
     * Returns an ImageUploader instance associated with the specified media provider
     *
     * @param mediaProvider media provider
     * @return ImageUploader
     */
    public ImageUpload getInstance(MediaProvider mediaProvider) {
        Authorization authorization = AuthorizationFactory.getInstance(conf);
        return getInstance(mediaProvider, authorization);
    }

    /**
     * Returns an ImageUpload instance associated with the specified media provider
     *
     * @param mediaProvider media provider
     * @param authorization authorization
     * @return ImageUpload
     * @since Twitter4J 2.1.11
     */
    public ImageUpload getInstance(MediaProvider mediaProvider, Authorization authorization) {
        if (!(authorization instanceof OAuthAuthorization)) {
            throw new IllegalArgumentException("OAuth authorization is required.");
        }
        OAuthAuthorization oauth = (OAuthAuthorization) authorization;
        if (mediaProvider == IMG_LY) {
            return new ImgLyUpload(conf, oauth);
        } else if (mediaProvider == PLIXI) {
            return new PlixiUpload(conf, apiKey, oauth);
        } else if (mediaProvider == LOCKERZ) {
            return new PlixiUpload(conf, apiKey, oauth);
        } else if (mediaProvider == TWIPPLE) {
            return new TwippleUpload(conf, oauth);
        } else if (mediaProvider == TWITGOO) {
            return new TwitgooUpload(conf, oauth);
        } else if (mediaProvider == YFROG) {
            return new YFrogUpload(conf, oauth);
        } else if (mediaProvider == MOBYPICTURE) {
            return new MobypictureUpload(conf, apiKey, oauth);
        } else if (mediaProvider == POSTEROUS) {
            return new PosterousUpload(conf, oauth);
        } else {
            throw new AssertionError("Unknown provider");
        }
    }
}
