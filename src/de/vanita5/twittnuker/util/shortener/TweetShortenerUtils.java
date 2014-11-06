/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
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
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * Some of the functions are originally from Luke Klinker, creator of Talon.
 * They are marked with a @author notation!
 *
 *
 *
 * Copyright 2014 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vanita5.twittnuker.util.shortener;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import de.vanita5.twittnuker.model.Account;
import de.vanita5.twittnuker.model.ParcelableStatusUpdate;
import de.vanita5.twittnuker.task.HototinAsyncTask;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.task.TwitlongerAsyncTask;
import de.vanita5.twittnuker.util.Utils;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuthAuthorization;
import twitter4j.http.BASE64Encoder;
import twitter4j.http.HttpParameter;
import twitter4j.internal.util.InternalStringUtil;

import static de.vanita5.twittnuker.util.Utils.getAccountScreenName;
import static de.vanita5.twittnuker.util.Utils.getAccountProfileImage;

public class TweetShortenerUtils implements Constants {

	public static final String OAUTH_SERVICE_PROVIDER = "https://api.twitter.com/1.1/account/verify_credentials.json";

	private static final String DEFAULT_AVATAR_URL = "https://twimg0-a.akamaihd.net/sticky/default_profile_images/default_profile_3_bigger.png";

	private static final String LOG_TAG = "TweetShortenerUtils.java";

	private static final String HOTOTIN_URL = "http://hotot.in/create.json";
	private static final String HOTOTIN_ENTITY_NAME = "name";
	private static final String HOTOTIN_ENTITY_AVATAR = "avatar";
	private static final String HOTOTIN_ENTITY_TEXT = "text";

	private static final String TWITLONGER_URL = "http://api.twitlonger.com/2/posts";
	private static final String TWITLONGER_ENTITY_CONTENT = "content";
	private static final String TWITLONGER_ENTITY_REPLY_TO_ID = "reply_to_id";
	private static final String TWITLONGER_ENTITY_REPLY_TO_SCREENNAME = "reply_to_screen_name";

	/**
	 * Shorten long tweets with hotot.in
	 * @param context
	 * @return shortened tweet
	 */
	public static Map<Long, ShortenedStatusModel> shortWithHototin(final Context context, final ParcelableStatusUpdate pstatus) {
		final Account[] accounts = pstatus.accounts;
		Map<Long, ShortenedStatusModel> statuses = new HashMap<>();

		for (Account account : accounts) {

			String screen_name = null;
			String avatar_url = null;
			try {
				screen_name = getAccountScreenName(context, account.account_id);
				avatar_url = getAccountProfileImage(context, account.account_id);
				avatar_url = avatar_url != null && !avatar_url.isEmpty() ? avatar_url : DEFAULT_AVATAR_URL;

				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(HOTOTIN_URL);
				MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				requestEntity.addPart(HOTOTIN_ENTITY_NAME, new StringBody(screen_name));
				requestEntity.addPart(HOTOTIN_ENTITY_AVATAR, new StringBody(avatar_url));
				requestEntity.addPart(HOTOTIN_ENTITY_TEXT, new StringBody(pstatus.text, Charset.forName("UTF-8")));
				httpPost.setEntity(requestEntity);

				InputStream responseStream;
				BufferedReader br;

				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity responseEntity = response.getEntity();
				responseStream = responseEntity.getContent();
				br = new BufferedReader(new InputStreamReader(responseStream));

				String responseLine = br.readLine();
				String tmpResponse = "";
				while (responseLine != null) {
					tmpResponse += responseLine + System.getProperty("line.separator");
					responseLine = br.readLine();
				}
				br.close();

				JSONObject jsonObject = new JSONObject(tmpResponse);

				String result = jsonObject.getString("text");

				statuses.put(account.account_id, new ShortenedStatusModel(result, null));
			} catch (Exception e) {
				if (Utils.isDebugBuild()) Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
		return statuses;
	}

	/**
	 * Post status to Twitlonger. Returns the postet statuses for each account
	 * @param context
	 * @param pstatus
	 * @return shortened tweet
	 */
	public static Map<Long, ShortenedStatusModel> postTwitlonger(final Context context, final ParcelableStatusUpdate pstatus) {
		final Account[] accounts = pstatus.accounts;
		Map<Long, ShortenedStatusModel> statuses = new HashMap<>();
		for (Account account : accounts) {
			Twitter twitter = Utils.getTwitterInstance(context, account.account_id, true);
			try {
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(TWITLONGER_URL);
				httpPost.addHeader("X-API-KEY", TWITLONGER_API_KEY);
				httpPost.addHeader("X-Auth-Service-Provider", OAUTH_SERVICE_PROVIDER);
				httpPost.addHeader("X-Verify-Credentials-Authorization", getHeader(twitter));

				List<NameValuePair> nvps = new ArrayList<>();
				nvps.add(new BasicNameValuePair(TWITLONGER_ENTITY_CONTENT, pstatus.text));

				if (pstatus.in_reply_to_status_id > 0) {
					nvps.add(new BasicNameValuePair(TWITLONGER_ENTITY_REPLY_TO_ID, String.valueOf(pstatus.in_reply_to_status_id)));
				}
				final String replyToScreenName = getReplyToScreenName(pstatus.text);
				if (replyToScreenName != null) {
					nvps.add(new BasicNameValuePair(TWITLONGER_ENTITY_REPLY_TO_SCREENNAME, (replyToScreenName)));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

				InputStream responseStream;
				BufferedReader br;

				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity responseEntity = response.getEntity();
				responseStream = responseEntity.getContent();
				br = new BufferedReader(new InputStreamReader(responseStream));

				String responseLine = br.readLine();
				String tmpResponse = "";
				while (responseLine != null) {
					tmpResponse += responseLine + System.getProperty("line.separator");
					responseLine = br.readLine();
				}
				br.close();

				String content;
				String id;
				try {
					JSONObject jsonObject = new JSONObject(tmpResponse);
					content = jsonObject.getString("tweet_content");
					id = jsonObject.getString("id");
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				statuses.put(account.account_id, new ShortenedStatusModel(content, id));
			} catch (Exception e) {
				if (Utils.isDebugBuild()) Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
				return null;
			}
		}
		return statuses;
	}

	public static void expandHototin(final Activity activity, final String url) {
		if (activity == null || url == null) return;
		new HototinAsyncTask(activity, url);
	}

	public static void expandTwitLonger(final Activity activity, final String url) {
		if (activity == null || url == null) return;
		new TwitlongerAsyncTask(activity, url);
	}

	/**
	 * Updates the status on twitlonger to include the tweet id from Twitter.
	 * @author Luke Klinker
	 * @param status
	 * @param tweetId
	 * @return true if the update is sucessful
	 */
	public static boolean updateTwitlonger(ShortenedStatusModel status, long tweetId, Twitter twitter) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPut put = new HttpPut(TWITLONGER_URL + "/" + status.getId());
			put.addHeader("X-API-KEY", TWITLONGER_API_KEY);
			put.addHeader("X-Auth-Service-Provider", OAUTH_SERVICE_PROVIDER);
			put.addHeader("X-Verify-Credentials-Authorization", getHeader(twitter));

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			nvps.add(new BasicNameValuePair("twitter_status_id", tweetId + ""));

			put.setEntity(new UrlEncodedFormEntity(nvps));
			HttpResponse response = client.execute(put);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			if (rd.readLine() != null) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Gets the header to verify the user on Twitter
	 * twitter4j
	 * @param twitter Coming from Twitter.getInstance()
	 * @return String of the header to be used with X-Verify-Credentials-Authorization
	 */
	public static String getHeader(Twitter twitter) {
		try {
			final long timestamp = System.currentTimeMillis() / 1000;
			Random rand = new Random();
			final long nonce = timestamp + rand.nextInt();

			final List<HttpParameter> oauthHeaderParams = new ArrayList<HttpParameter>(5);
			oauthHeaderParams.add(new HttpParameter("oauth_consumer_key", TWITTER_CONSUMER_KEY_2));
			oauthHeaderParams.add(new HttpParameter("oauth_signature_method", "HMAC-SHA1"));
			oauthHeaderParams.add(new HttpParameter("oauth_timestamp", timestamp));
			oauthHeaderParams.add(new HttpParameter("oauth_nonce", nonce));
			oauthHeaderParams.add(new HttpParameter("oauth_version", "1.0"));
			if (twitter.getOAuthAccessToken() != null) {
				oauthHeaderParams.add(new HttpParameter("oauth_token", twitter.getOAuthAccessToken().getToken()));
			}

			final List<HttpParameter> signatureBaseParams = new ArrayList<HttpParameter>(oauthHeaderParams.size());
			signatureBaseParams.addAll(oauthHeaderParams);
			parseGetParameters(OAUTH_SERVICE_PROVIDER, signatureBaseParams);

			final StringBuffer base = new StringBuffer("GET").append("&")
					.append(HttpParameter.encode(constructRequestURL(OAUTH_SERVICE_PROVIDER))).append("&");
			base.append(HttpParameter.encode(normalizeRequestParameters(signatureBaseParams)));

			final String oauthBaseString = base.toString();
			final String signature = generateSignature(oauthBaseString, twitter.getOAuthAccessToken());

			oauthHeaderParams.add(new HttpParameter("oauth_signature", signature));

			return "OAuth realm=\"http://api.twitter.com/\"," + OAuthAuthorization.encodeParameters(oauthHeaderParams, ",", true);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * twitter4j
	 * @param url
	 * @param signatureBaseParams
	 */
	private static void parseGetParameters(final String url, final List<HttpParameter> signatureBaseParams) {
		final int queryStart = url.indexOf("?");
		if (-1 != queryStart) {
			final String[] queryStrs = InternalStringUtil.split(url.substring(queryStart + 1), "&");
			try {
				for (final String query : queryStrs) {
					final String[] split = InternalStringUtil.split(query, "=");
					if (split.length == 2) {
						signatureBaseParams.add(new HttpParameter(URLDecoder.decode(split[0], "UTF-8"), URLDecoder
								.decode(split[1], "UTF-8")));
					} else {
						signatureBaseParams.add(new HttpParameter(URLDecoder.decode(split[0], "UTF-8"), ""));
					}
				}
			} catch (final UnsupportedEncodingException ignore) {
			}

		}

	}

	/**
	 * Generates the signature to use with the header
	 * @author Luke Klinker
	 * @param data base signature data
	 * @param token the user's access token
	 * @return String of the signature to use in your header
	 */
	public static String generateSignature(String data, AccessToken token) {
		byte[] byteHMAC = null;
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec spec;
			String oauthSignature = HttpParameter.encode(TWITTER_CONSUMER_SECRET_2) + "&" + HttpParameter.encode(token.getTokenSecret());
			spec = new SecretKeySpec(oauthSignature.getBytes(), "HmacSHA1");
			mac.init(spec);
			byteHMAC = mac.doFinal(data.getBytes());
		} catch (InvalidKeyException ike) {
			throw new AssertionError(ike);
		} catch (NoSuchAlgorithmException nsae) {
			throw new AssertionError(nsae);
		}
		return BASE64Encoder.encode(byteHMAC);
	}

	/**
	 * Sorts and prepares the parameters
	 * @author Luke Klinker
	 * @param params Your parameters to post
	 * @return String of the encoded parameters
	 */
	static String normalizeRequestParameters(List<HttpParameter> params) {
		Collections.sort(params);
		return encodeParameters(params, "&", false);
	}

	/**
	 * Encodes the parameters
	 * @author Luke Klinker
	 * @param httpParams parameters you want to send
	 * @param splitter character used to split the parameters
	 * @param quot whether you should use quotations or not
	 * @return string of the desired encoding
	 */
	public static String encodeParameters(List<HttpParameter> httpParams, String splitter, boolean quot) {
		StringBuilder buf = new StringBuilder();
		for (HttpParameter param : httpParams) {
			if (!param.isFile()) {
				if (buf.length() != 0) {
					if (quot) {
						buf.append("\"");
					}
					buf.append(splitter);
				}
				buf.append(HttpParameter.encode(param.getName())).append("=");
				if (quot) {
					buf.append("\"");
				}
				buf.append(HttpParameter.encode(param.getValue()));
			}
		}
		if (buf.length() != 0) {
			if (quot) {
				buf.append("\"");
			}
		}
		return buf.toString();
	}

	/**
	 * Used to create the base signature text
	 * @author Luke Klinker (Talon)
	 * @param url url of the post
	 * @return string of the base signature
	 */
	static String constructRequestURL(String url) {
		int index = url.indexOf("?");
		if (-1 != index) {
			url = url.substring(0, index);
		}
		int slashIndex = url.indexOf("/", 8);
		String baseURL = url.substring(0, slashIndex).toLowerCase();
		int colonIndex = baseURL.indexOf(":", 8);
		if (-1 != colonIndex) {
			// url contains port number
			if (baseURL.startsWith("http://") && baseURL.endsWith(":80")) {
				// http default port 80 MUST be excluded
				baseURL = baseURL.substring(0, colonIndex);
			} else if (baseURL.startsWith("https://") && baseURL.endsWith(":443")) {
				// http default port 443 MUST be excluded
				baseURL = baseURL.substring(0, colonIndex);
			}
		}
		url = baseURL + url.substring(slashIndex);

		return url;
	}

	/**
	 * In case of a reply we have to supply the reply_to_screen_name
	 * We just assume, that the first mention in the tweet is the right one-
	 * @param text
	 * @return screen_name
	 */
	private static String getReplyToScreenName(String text) {
		String[] parts = text.split(" ");
		for(String part : parts) {
			if (part.startsWith("@")) {
				return part.substring(1);
			}
		}
		return null;
	}

	/**
	 * @author Luke Klinker
	 */
	public static class ShortenedStatusModel {
		private String text;
		private String id;

		public ShortenedStatusModel(String text, String id) {
			this.text = text;
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public String getText() {
			return text;
		}
	}
}
