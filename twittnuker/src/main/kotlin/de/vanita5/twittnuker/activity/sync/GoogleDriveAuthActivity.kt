/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.activity.sync

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.DriveScopes
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.kpreferences.set
import de.vanita5.twittnuker.activity.BaseActivity
import de.vanita5.twittnuker.constant.dataSyncProviderInfoKey
import de.vanita5.twittnuker.model.sync.GoogleDriveSyncProviderInfo


class GoogleDriveAuthActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {


    private lateinit var googleApiClient: GoogleApiClient

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
                .requestServerAuthCode(GoogleDriveSyncProviderInfo.WEB_CLIENT_ID, true)
                .build()

        googleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleApiClient.connect();
    }

    override fun onDestroy() {
        googleApiClient.disconnect()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_RESOLVE_ERROR -> {
                if (!googleApiClient.isConnected && !googleApiClient.isConnecting) {
                    googleApiClient.connect()
                }
            }
            REQUEST_GOOGLE_SIGN_IN -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                    val authCode = result.signInAccount?.serverAuthCode ?: return
                    val httpTransport = NetHttpTransport()
                    val jsonFactory = JacksonFactory.getDefaultInstance()
                    val tokenRequest = GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,
                            "https://www.googleapis.com/oauth2/v4/token", GoogleDriveSyncProviderInfo.WEB_CLIENT_ID,
                            GoogleDriveSyncProviderInfo.WEB_CLIENT_SECRET, authCode, "")
                    task {
                        tokenRequest.execute()
                    }.successUi { response ->
                        preferences[dataSyncProviderInfoKey] = GoogleDriveSyncProviderInfo(response.refreshToken)
                        setResult(Activity.RESULT_OK)
                        finish()
                    }.fail { ex ->
                        ex.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onConnected(connectionHint: Bundle?) {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
            // Start sign in
            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
            startActivityForResult(signInIntent, REQUEST_GOOGLE_SIGN_IN)
        }
    }

    override fun onConnectionSuspended(cause: Int) {
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (connectionResult.hasResolution()) {
            connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR)
        } else {

        }
    }

    companion object {

        private const val REQUEST_RESOLVE_ERROR: Int = 101
        private const val REQUEST_GOOGLE_SIGN_IN: Int = 102
    }
}