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

package de.vanita5.twittnuker.util.imageloader;

import static de.vanita5.twittnuker.util.TwidereLinkify.PATTERN_TWITTER_PROFILE_IMAGES;
import static de.vanita5.twittnuker.util.TwidereLinkify.TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES;
import static de.vanita5.twittnuker.util.Utils.generateBrowserUserAgent;
import static de.vanita5.twittnuker.util.Utils.getImageLoaderHttpClient;
import static de.vanita5.twittnuker.util.Utils.getProxy;
import static de.vanita5.twittnuker.util.Utils.getRedirectedHttpResponse;
import static de.vanita5.twittnuker.util.Utils.replaceLast;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.download.ImageDownloader;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.util.MediaPreviewUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.io.ContentLengthInputStream;


import twitter4j.TwitterException;
import twitter4j.auth.Authorization;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TwidereImageDownloader implements ImageDownloader, Constants {

	private static final HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();
	private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] { new TrustAllX509TrustManager() };
	private static final SSLSocketFactory IGNORE_ERROR_SSL_FACTORY;

	static {
		System.setProperty("http.keepAlive", "false");
		SSLSocketFactory factory = null;
		try {
			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, TRUST_ALL_CERTS, new SecureRandom());
			factory = sc.getSocketFactory();
		} catch (final KeyManagementException e) {
		} catch (final NoSuchAlgorithmException e) {
		}
		IGNORE_ERROR_SSL_FACTORY = factory;
	}

	private final Context mContext;
	private final SharedPreferences mPreferences;
	private final ContentResolver mResolver;
	private HttpClientWrapper mClient;
	private Proxy mProxy;
	private boolean mFastImageLoading;
	private String mUserAgent;
	private final boolean mFullImage;
	private final String mTwitterProfileImageSize;

	public TwidereImageDownloader(final Context context, final boolean fullImage) {
		mContext = context;
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = context.getContentResolver();
		mFullImage = fullImage;
		mTwitterProfileImageSize = String.format("_%s", context.getString(R.string.profile_image_size));
		reloadConnectivitySettings();
	}

	@Override
	public InputStream getStream(final String uriString, final Object extras) throws IOException {
		if (uriString == null) return null;
		final Uri uri = Uri.parse(uriString);
		final String scheme = uri.getScheme();
		if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_CONTENT.equals(scheme)
				|| ContentResolver.SCHEME_FILE.equals(scheme)) return mResolver.openInputStream(uri);
		final ParcelableMedia media = MediaPreviewUtils.getAllAvailableImage(uriString, mFullImage, mFullImage
				|| !mFastImageLoading ? mClient : null);
		try {
			final String mediaUrl = media != null ? media.media_url : uriString;
			if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(uriString).matches()) {
				final String replaced = replaceLast(mediaUrl, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES,
						mTwitterProfileImageSize);
				return getStreamFromNetwork(replaced, extras);
			} else
				return getStreamFromNetwork(mediaUrl, extras);
		} catch (final TwitterException e) {
			final int statusCode = e.getStatusCode();
			if (statusCode != -1 && PATTERN_TWITTER_PROFILE_IMAGES.matcher(uriString).matches()
					&& !uriString.contains("_normal.")) {
				try {
					return getStreamFromNetwork(Utils.getNormalTwitterProfileImage(uriString), extras);
				} catch (final TwitterException e2) {

				}
			}
			throw new IOException(String.format(Locale.US, "Error downloading image %s, error code: %d", uriString,
									statusCode));
		}
	}

	public void reloadConnectivitySettings() {
		mClient = getImageLoaderHttpClient(mContext);
		mFastImageLoading = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(
				KEY_FAST_IMAGE_LOADING, true);
		mProxy = getProxy(mContext);
		mUserAgent = generateBrowserUserAgent();
	}

	private String getReplacedTwitterHost(final String host) {
		if (host == null || !host.endsWith("twitter.com")) return host;
		final String jtapiHostname = mPreferences.getString(KEY_JTAPI_HOSTNAME, null);
		if (TextUtils.isEmpty(jtapiHostname)) return host;
		return Utils.replaceLast(host, "twitter\\.com", jtapiHostname);
	}

	private String getReplacedUri(final String uri, final String scheme, final String host) {
		final String replacedHost = getReplacedTwitterHost(host);
		final String target = Pattern.quote(String.format("%s://%s", scheme, host));
		return uri.replaceFirst(target, String.format("%s://%s", scheme, replacedHost));
	}

	private ContentLengthInputStream getStreamFromNetwork(final String uriString, final Object extras)
			throws IOException, TwitterException {
		final URL url = new URL(uriString);
		final Authorization auth;
		if (isOAuthRequired(url) && extras instanceof AccountExtra) {
			final AccountExtra accountExtra = (AccountExtra) extras;
			auth = Utils.getTwitterAuthorization(mContext, accountExtra.account_id);
		} else {
			auth = null;
		}
		final String modifiedUri = getReplacedUri(uriString, url.getProtocol(), url.getHost());
		final HttpResponse resp = getRedirectedHttpResponse(mClient, modifiedUri, uriString, auth);
		return new ContentLengthInputStream(resp.asStream(), (int) resp.getContentLength());
	}

	private boolean isOAuthRequired(final URL url) {
		if (url == null) return false;
		return "ton.twitter.com".equalsIgnoreCase(url.getHost());
	}

	private static final class AllowAllHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(final String hostname, final SSLSession session) {
			return true;
		}
	}

	private static final class TrustAllX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
		}

		@Override
		public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}
}
