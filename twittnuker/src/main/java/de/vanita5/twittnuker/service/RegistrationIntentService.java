/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
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

package de.vanita5.twittnuker.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.constant.IntentConstants;
import de.vanita5.twittnuker.constant.SharedPreferenceConstants;

public class RegistrationIntentService extends IntentService implements Constants {

    private SharedPreferences mPreferences;

    private static final String TAG = "RegIntentService";
    private static final String TOPIC = "/topics/global";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);

            //R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(TAG, "GCM Registration Token: " + token);

            sendRegistrationToServer(token);
            subscribeGlobal(token);
        } catch (Exception e) {
            mPreferences.edit().putBoolean(SharedPreferenceConstants.GCM_TOKEN_SENT, false).apply();
            Log.e(TAG, "GCM Registration failed.", e);
        }
        sendBroadcast(new Intent(IntentConstants.GCM_REGISTRATION_COMPLETE));
    }

    /**
     * Send regitration token to backend server if available
     *
     * @param token
     */
    private void sendRegistrationToServer(final String token) {
        //TODO send to backend server

        //TODO on success: remember backend received token
        mPreferences.edit().putBoolean(SharedPreferenceConstants.GCM_TOKEN_SENT, true).apply();
    }

    private void subscribeGlobal(final String token) {
//        GcmPubSub pubSub = GcmPubSub.getInstance(this);
//        try {
//            pubSub.subscribe(token, TOPIC, null);
//        } catch (IOException e) {
//            mPreferences.edit().putBoolean(SharedPreferenceConstants.GCM_TOKEN_SENT, false).apply();
//            Log.e(TAG, "Subscribing to backend server global topic failed.", e);
//        }
    }
}
