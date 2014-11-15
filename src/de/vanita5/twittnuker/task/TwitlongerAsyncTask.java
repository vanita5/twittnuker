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

public class TwitlongerAsyncTask implements Constants {

	public static final String TWITLONGER_URL = "http://api.twitlonger.com/2/posts/";

	private static final String TWITLONGER_ENTITY_CONTENT = "content";

	private final Context mContext;
	private final String mUrl;

	public TwitlongerAsyncTask(final Context context, final String url) {
		super();
		this.mContext = context;
		this.mUrl = url;
		this.doExpand(url);
	}

	protected void doExpand(String url) {
		url = url.replace("http://tl.gd/", "");
		url = url.replace("http://twitlonger.com/show/", "");
		final String finalUrl = TWITLONGER_URL + url;

		Thread thread = new Thread() {

			@Override
			public void run() {
				try {
					HttpClient httpClient = HttpClientFactory.getThreadSafeClient();
					HttpGet httpGet = new HttpGet(finalUrl);
					httpGet.addHeader("X-API-KEY", TWITLONGER_API_KEY);

					HttpResponse response = httpClient.execute(httpGet);

					String sResponse = EntityUtils.toString(response.getEntity());

					JSONObject jsonObject = new JSONObject(sResponse);

					String result = jsonObject.getString(TWITLONGER_ENTITY_CONTENT);

					final Intent intent = new Intent(BROADCAST_TWITLONGER_EXPANDED);
					intent.putExtra(EXTRA_TWITLONGER_EXPANDED_TEXT, result);
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
