package de.vanita5.twittnuker.task;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.Utils;

public class GetGCMTokenTask extends AsyncTask<Void, Void, String> implements Constants {

	private static final String TAG = "GetGCMTokenTask";

	public static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
	public static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;

	//TODO Replace activity by specific activity that handles
	protected Activity mActivity;
	protected String mScope;
	protected String mEmail;

	private SharedPreferencesWrapper mPreferences;

	public GetGCMTokenTask(Activity activity, String email, String scope) {
		mActivity = activity;
		mEmail = email;
		mScope = scope;
		mPreferences = SharedPreferencesWrapper.getInstance(activity, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	@Override
	protected String doInBackground(Void... params) {
		String token = null;
		try {
			token = fetchToken();
		} catch (IOException e) {
			if (Utils.isDebugBuild()) {
				Log.e(TAG, "Exception: ", e);
			}
		}
		if (token != null) {
			//TODO
			mPreferences.edit().putString(KEY_GOOGLE_ACCOUNT, mEmail).commit();
		}
		return token;
	}

	/**
	 * Get a authentication token if one is not available. If the error is not
	 * recoverable then it displays the error message on parent activity.
	 */
	protected String fetchToken() throws IOException {
		try {
			return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
		}
		catch (GooglePlayServicesAvailabilityException e) {
			// GooglePlayServices.apk is either old, disabled, or not present.
			showErrorDialog(e.getConnectionStatusCode());
		}
		catch (UserRecoverableAuthException e) {
			// Unable to authenticate, but the user can fix this.
			// Forward the user to the appropriate activity.
			mActivity.startActivityForResult(e.getIntent(),
					REQUEST_CODE_RECOVER_FROM_AUTH_ERROR);
		}
		catch (GoogleAuthException e) {
			if (Utils.isDebugBuild()) {
				Log.e(TAG, "Unrecoverable error", e);
			}
		}
		return null;
	}

	private void showErrorDialog(final int code) {
		mActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Dialog d = GooglePlayServicesUtil.getErrorDialog(
						code,
						mActivity,
						GetGCMTokenTask.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
				d.show();
			}
		});
	}
}
