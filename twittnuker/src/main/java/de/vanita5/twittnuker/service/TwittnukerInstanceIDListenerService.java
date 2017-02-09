/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

import de.vanita5.twittnuker.TwittnukerConstants;
import de.vanita5.twittnuker.constant.SharedPreferenceConstants;
import de.vanita5.twittnuker.push.PushBackendServer;

public class TwittnukerInstanceIDListenerService extends InstanceIDListenerService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify the backend server of any changes.
        Log.d("InstanceIDListener", "Refresh GMC Token");

        SharedPreferences sharedPreferences = getSharedPreferences(TwittnukerConstants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        final String currentToken = sharedPreferences.getString(SharedPreferenceConstants.GCM_CURRENT_TOKEN, null);
        if (!TextUtils.isEmpty(currentToken)) {
            PushBackendServer backend = new PushBackendServer(this);
            if (backend.remove(currentToken)) {
                sharedPreferences.edit().putBoolean(SharedPreferenceConstants.GCM_TOKEN_SENT, false).apply();
                sharedPreferences.edit().putString(SharedPreferenceConstants.GCM_CURRENT_TOKEN, null).apply();

                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }
        }
    }
}
