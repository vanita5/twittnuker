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

package de.vanita5.twittnuker.service

import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.util.SimpleArrayMap
import android.util.Log
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import de.vanita5.twittnuker.library.twitter.TwitterUserStream
import de.vanita5.twittnuker.library.twitter.UserStreamCallback
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.activity.SettingsActivity
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.AccountPreferences
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.DebugLog
import de.vanita5.twittnuker.util.TwidereArrayUtils

class StreamingService : Service() {

    private val callbacks = SimpleArrayMap<UserKey, UserStreamCallback>()

    private var notificationManager: NotificationManager? = null

    private var accountKeys: Array<UserKey>? = null

    private val accountChangeObserver = OnAccountsUpdateListener {
        if (!TwidereArrayUtils.contentMatch(accountKeys, DataStoreUtils.getActivatedAccountKeys(this@StreamingService))) {
            initStreaming()
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        DebugLog.d(LOGTAG, "Stream service started.")
        initStreaming()
        AccountManager.get(this).addOnAccountsUpdatedListenerSafe(accountChangeObserver, updateImmediately = false)
    }

    override fun onDestroy() {
        clearTwitterInstances()
        AccountManager.get(this).removeOnAccountsUpdatedListenerSafe(accountChangeObserver)
        DebugLog.d(LOGTAG, "Stream service stopped.")
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun clearTwitterInstances() {
        var i = 0
        val j = callbacks.size()
        while (i < j) {
            Thread(ShutdownStreamTwitterRunnable(callbacks.valueAt(i))).start()
            i++
        }
        callbacks.clear()
        notificationManager!!.cancel(NOTIFICATION_SERVICE_STARTED)
    }

    private fun initStreaming() {
        if (!BuildConfig.DEBUG) return
        setTwitterInstances()
        updateStreamState()
    }

    private fun setTwitterInstances(): Boolean {
        val accountsList = AccountUtils.getAllAccountDetails(AccountManager.get(this), true).filter { it.credentials is OAuthCredentials }
        val accountKeys = accountsList.map { it.key }.toTypedArray()
        val activatedPreferences = AccountPreferences.getAccountPreferences(this, accountKeys)
        DebugLog.d(LOGTAG, "Setting up twitter stream instances")
        this.accountKeys = accountKeys
        clearTwitterInstances()
        var result = false
        accountsList.forEachIndexed { i, account ->
            val preferences = activatedPreferences[i]
            if (!preferences.isStreamingEnabled) {
                return@forEachIndexed
            }
            val twitter = account.newMicroBlogInstance(context = this, cls = TwitterUserStream::class.java)
            val callback = TwidereUserStreamCallback(this, account)
            callbacks.put(account.key, callback)
            object : Thread() {
                override fun run() {
                    twitter.getUserStream("user", callback)
                    Log.d(LOGTAG, String.format("Stream %s disconnected", account.key))
                    callbacks.remove(account.key)
                    updateStreamState()
                }
            }.start()
            result = result or true
        }
        return result
    }

    private fun updateStreamState() {
        if (callbacks.size() > 0) {
            val intent = Intent(this, SettingsActivity::class.java)
            val contentIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT)
            val contentTitle = getString(R.string.app_name)
            val contentText = getString(R.string.streaming_service_running)
            val builder = NotificationCompat.Builder(this)
            builder.setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_stat_twittnuker)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setTicker(getString(R.string.streaming_service_running))
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setContentIntent(contentIntent)
            notificationManager!!.notify(NOTIFICATION_SERVICE_STARTED, builder.build())
        } else {
            notificationManager!!.cancel(NOTIFICATION_SERVICE_STARTED)
        }
    }

    internal class ShutdownStreamTwitterRunnable(private val callback: UserStreamCallback?) : Runnable {

        override fun run() {
            if (callback == null) return
            Log.d(LOGTAG, "Disconnecting stream")
            callback.disconnect()
        }

    }

    internal class TwidereUserStreamCallback(
            private val context: Context,
            private val account: AccountDetails
    ) : UserStreamCallback() {

        private var statusStreamStarted: Boolean = false
        private val mentionsStreamStarted: Boolean = false

    }

    companion object {

        private val NOTIFICATION_SERVICE_STARTED = 1

    }

}