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
 */

package de.vanita5.twittnuker.util.shortener;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import de.vanita5.twittnuker.task.HototinAsyncTask;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.Utils;

import static de.vanita5.twittnuker.util.Utils.getAccountScreenName;
import static de.vanita5.twittnuker.util.Utils.getAccountProfileImage;

public class TweetShortenerUtils implements Constants {

	private static final String DEFAULT_AVATAR_URL = "https://twimg0-a.akamaihd.net/sticky/default_profile_images/default_profile_3_bigger.png";

	private static final String LOG_TAG = "TweetShortenerUtils.java";

	private static final String HOTOTIN_URL = "http://hotot.in/create.json";
	private static final String HOTOTIN_ENTITY_NAME = "name";
	private static final String HOTOTIN_ENTITY_AVATAR = "avatar";
	private static final String HOTOTIN_ENTITY_TEXT = "text";

	/**
	 * Shorten long tweets with hotot.in
	 * @param context
	 * @param text
	 * @param account_ids
	 * @return shortened tweet
	 */
	public static String shortWithHototin(final Context context, final String text, final long[] account_ids) {

		String screen_name = null;
		String avatar_url = null;

		if(account_ids != null && account_ids.length > 0) {
			screen_name = getAccountScreenName(context, account_ids[0]);
			avatar_url = getAccountProfileImage(context, account_ids[0]);
			avatar_url = avatar_url != null && !avatar_url.isEmpty() ? avatar_url : DEFAULT_AVATAR_URL;
		}

		try {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(HOTOTIN_URL);
			MultipartEntity requestEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			requestEntity.addPart(HOTOTIN_ENTITY_NAME, new StringBody(screen_name));
			requestEntity.addPart(HOTOTIN_ENTITY_AVATAR, new StringBody(avatar_url));
			requestEntity.addPart(HOTOTIN_ENTITY_TEXT, new StringBody(text, Charset.forName("UTF-8")));
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

			return result;

		} catch (UnsupportedEncodingException e) {
			if (Utils.isDebugBuild()) Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			if (Utils.isDebugBuild()) Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			if (Utils.isDebugBuild()) Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		} catch (JSONException e) {
			if (Utils.isDebugBuild()) Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public static void expandHototin(final Activity activity, final String url) {
		if (activity == null || url == null) return;
		final HototinAsyncTask task = new HototinAsyncTask(activity, url);
	}
}
