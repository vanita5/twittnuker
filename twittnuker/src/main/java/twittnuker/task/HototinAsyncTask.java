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

package de.vanita5.twittnuker.task;

import android.content.Context;
import android.content.Intent;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.HttpClientFactory;

public class HototinAsyncTask implements Constants {

	private static final String HOTOT_URL = "http://hotot.in/tweet/";

	private static final String HOTOT_ENTITY_FULL_TEXT = "full_text";

	private final Context mContext;
	private final String mUrl;

	public HototinAsyncTask(final Context context, final String url) {
		super();
		this.mContext = context;
		this.mUrl = url;
		this.doExpand(url);
	}

	protected void doExpand(final String url) {
		final String finalUrl = HOTOT_URL + url.replace("http://hotot.in/", "") + ".json";

		Thread thread = new Thread() {

            @Override
			public void run() {
				try {
					HttpClient httpClient = HttpClientFactory.getThreadSafeClient();
					HttpResponse response = httpClient.execute(new HttpGet(finalUrl));

					String sResponse = EntityUtils.toString(response.getEntity());

					JSONObject jsonObject = new JSONObject(sResponse);

					String result = jsonObject.getString(HOTOT_ENTITY_FULL_TEXT);

					final Intent intent = new Intent(BROADCAST_HOTOTIN_EXPANDED);
					intent.putExtra(EXTRA_HOTOTIN_EXPANDED_TEXT, result);
					mContext.sendBroadcast(intent);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
}
