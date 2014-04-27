package de.vanita5.twittnuker.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableNotifiedException;

import java.io.IOException;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.gcm.backend.PushBackendServer;
import retrofit.RestAdapter;

public class PushBackendHelper implements TwittnukerConstants {

	public static final String TAG = "PushBackendHelper";

	public static final String SCOPE = "oauth2:https://www.googleapis.com/auth/userinfo.email";
	public static final String AUTHORITY = "de.vanita5.twittnuker.gcm.backend.AUTHORITY";

	public static String getApiURL(final Context context) {
		final SharedPreferencesWrapper wrapper = SharedPreferencesWrapper
				.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String url = wrapper.getString(KEY_PUSH_API_URL, "");
		final String port = wrapper.getString(KEY_PUSH_API_PORT, "");
		return (url.startsWith("http") ? url : "http://" + url) + ":" + port;
	}

	public static PushBackendServer getRESTAdapter(final Context context) {
		RestAdapter restAdapter = new RestAdapter.Builder().setServer(getApiURL(context)).build();
		return restAdapter.create(PushBackendServer.class);
	}

	public static String getSavedAccountName(final Context context) {
		return SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.getString(KEY_GOOGLE_ACCOUNT, null);
	}

	public static String getAuthToken(final Context context) {
		final String accountName = getSavedAccountName(context);
		if (accountName == null || accountName.isEmpty()) {
			return null;
		}
		return getAuthToken(context, accountName);
	}

	/**
	 * Only use this in a background thread
	 */
	public static String getAuthToken(final Context context, final String accountName) {
		try {
			return "Bearer " + GoogleAuthUtil.getTokenWithNotification(context,
					accountName, SCOPE, null, AUTHORITY, null);
		}
		catch (UserRecoverableNotifiedException e) {
			// Unable to authenticate, but the user can fix this.
			if (Utils.isDebugBuild()) Log.e(TAG, "Could not fetch token: " + e.getMessage());
		}
		catch (GoogleAuthException e) {
			if (Utils.isDebugBuild()) Log.e(TAG, "Unrecoverable error " + e.getMessage());
		}
		catch (IOException e) {
			if (Utils.isDebugBuild()) Log.e(TAG, e.getMessage());
		}
		return null;
	}

	public static Account getAccount(final Context context, final String accountName) {
		if (accountName == null) return null;
		final AccountManager manager = AccountManager.get(context);
		Account[] accounts = manager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
		if (accounts != null) {
			for (Account account : accounts) {
				if (account == null) continue;
				if (accountName.equals(account.name)) {
					return account;
				}
			}
		}
		return null;
	}
}
