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

package de.vanita5.twittnuker.push;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import de.vanita5.twittnuker.TwittnukerConstants;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class PushBackendServer implements TwittnukerConstants {

    private SharedPreferences mSharedPreferences;

    private static final String STATUS_OK = "Ok";

    public PushBackendServer(final Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean register(String userId, String token) {
        String baseUrl = mSharedPreferences.getString(KEY_PUSH_API_URL, null);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(token)) return false;

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
        PushServerAPI api = retrofit.create(PushServerAPI.class);

        Call<Status> call = api.register(token, userId);
        try {
            Response<Status> response = call.execute();
            if (response.isSuccessful()) {
                Status status = response.body();
                return status != null && STATUS_OK.equals(status.getStatus());
            }
        } catch (IOException e) {
            Log.e("PushBackendServer", "Connecting failed", e);
        }
        return false;
    }

    public boolean remove(String token) {
        String baseUrl = mSharedPreferences.getString(KEY_PUSH_API_URL, null);
        if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(token)) return false;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        PushServerAPI api = retrofit.create(PushServerAPI.class);

        Call<Status> call = api.remove(token);
        try {
            Response<Status> response = call.execute();
            if (response.isSuccessful()) {
                Status status = response.body();
                return status != null && STATUS_OK.equals(status.getStatus());
            }
        } catch (IOException e) {
            Log.e("PushBackendServer", "Connecting failed", e);
        }
        return false;
    }

    private interface PushServerAPI {

        @FormUrlEncoded
        @POST("register")
        Call<Status> register(@Field("token") String token, @Field("userId") String userId);

        @FormUrlEncoded
        @POST("remove")
        Call<Status> remove(@Field("token") String token);
    }

    private class Status {
        String status;

        public String getStatus() {
            return status;
        }
    }
}
