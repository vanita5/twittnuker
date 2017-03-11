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
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.util.SimpleArrayMap
import android.text.TextUtils
import android.util.Log
import org.mariotaku.ktextension.addOnAccountsUpdatedListenerSafe
import org.mariotaku.ktextension.removeOnAccountsUpdatedListenerSafe
import de.vanita5.twittnuker.library.twitter.TwitterUserStream
import de.vanita5.twittnuker.library.twitter.UserStreamCallback
import de.vanita5.twittnuker.library.twitter.model.DeletionEvent
import de.vanita5.twittnuker.library.twitter.model.Status
import de.vanita5.twittnuker.library.twitter.model.User
import de.vanita5.twittnuker.library.twitter.model.Warning
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.TwittnukerConstants
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.activity.SettingsActivity
import de.vanita5.twittnuker.constant.SharedPreferenceConstants
import de.vanita5.twittnuker.extension.model.newMicroBlogInstance
import de.vanita5.twittnuker.model.*
import de.vanita5.twittnuker.model.account.cred.OAuthCredentials
import de.vanita5.twittnuker.model.util.AccountUtils
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils
import de.vanita5.twittnuker.provider.TwidereDataStore.*
import de.vanita5.twittnuker.util.*
import java.io.IOException

class StreamingService : Service() {

    private val callbacks = SimpleArrayMap<UserKey, UserStreamCallback>()

    private var notificationManager: NotificationManager? = null

    private var preferences: SharedPreferencesWrapper? = null

    private var accountKeys: Array<UserKey>? = null

    private val accountChangeObserver = OnAccountsUpdateListener {
        if (!TwidereArrayUtils.contentMatch(accountKeys, DataStoreUtils.getActivatedAccountKeys(this@StreamingService))) {
            initStreaming()
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferences = SharedPreferencesWrapper.getInstance(this, TwittnukerConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
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
            val callback = TwidereUserStreamCallback(this, account, this.preferences!!)
            callbacks.put(account.key, callback)
            object : Thread() {
                override fun run() {
                    twitter.getUserStream(callback)
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
            private val account: AccountDetails,
            private val mPreferences: SharedPreferences
    ) : UserStreamCallback() {

        private var statusStreamStarted: Boolean = false
        private val mentionsStreamStarted: Boolean = false

        private val mNotificationHelper: NotificationHelper = NotificationHelper(context)

        private fun createNotification(fromUser: String, type: String, msg: String?,
                                       status: ParcelableStatus?, sourceUser: User?) {
            if (mPreferences.getBoolean(SharedPreferenceConstants.KEY_STREAMING_NOTIFICATIONS, true)) {
                val pref = AccountPreferences(context,
                        account.key)
                val notification = NotificationContent()
                notification.accountKey = account.key
                notification.objectId = status?.id
                notification.objectUserKey = status?.user_key
                notification.fromUser = fromUser
                notification.type = type
                notification.message = msg
                notification.timestamp = status?.timestamp ?: System.currentTimeMillis()
                notification.originalStatus = status
                notification.sourceUser = sourceUser
                notification.profileImageUrl = if (sourceUser != null)
                    sourceUser.profileImageUrl
                else
                    status?.user_profile_image_url

                mNotificationHelper.cachePushNotification(notification)
                mNotificationHelper.buildNotificationByType(notification, pref, false)
            }
        }

        override fun onConnected() = true

        override fun onBlock(source: User, blockedUser: User): Boolean {
            val message = String.format("%s blocked %s", source.screenName, blockedUser.screenName)
            Log.d(LOGTAG, message)
            return true
        }

        override fun onDirectMessageDeleted(event: DeletionEvent): Boolean {
            val where = Expression.equalsArgs(Messages.MESSAGE_ID).sql
            val whereArgs = arrayOf(event.id)
            context.contentResolver.delete(Messages.CONTENT_URI, where, whereArgs)
            return true
        }

        override fun onStatusDeleted(event: DeletionEvent): Boolean {
            val statusId = event.id
            context.contentResolver.delete(Statuses.CONTENT_URI, Expression.equalsArgs(Statuses.STATUS_ID).sql,
                    arrayOf(statusId))
            context.contentResolver.delete(Activities.AboutMe.CONTENT_URI, Expression.equalsArgs(Activities.STATUS_ID).sql,
                    arrayOf(statusId))
            return true
        }

        override fun onFavorite(source: User, target: User, targetStatus: Status): Boolean {
            val message = String.format("%s favorited %s's tweet: %s", source.screenName,
                    target.screenName, targetStatus.extendedText)
            Log.d(LOGTAG, message)

            if (TextUtils.equals(targetStatus.user.id, account.key.id)) {
                createNotification(source.screenName, NotificationContent.NOTIFICATION_TYPE_FAVORITE,
                        Utils.parseURLEntities(targetStatus.extendedText, targetStatus.urlEntities),
                        ParcelableStatusUtils.fromStatus(targetStatus,
                                account.key, false),
                        source)
            }
            return true
        }

        override fun onFollow(source: User, followedUser: User): Boolean {
            val message = String
                    .format("%s followed %s", source.screenName, followedUser.screenName)
            Log.d(LOGTAG, message)
            if (TextUtils.equals(followedUser.id, account.key.id)) {
                createNotification(source.screenName, NotificationContent.NOTIFICATION_TYPE_FOLLOWER,
                        null, null, source)
            }
            return true
        }

        override fun onFriendList(friendIds: Array<String>): Boolean {
            return true
        }

        override fun onScrubGeo(userId: String, upToStatusId: String): Boolean {
            val resolver = context.contentResolver

            val where = Expression.and(Expression.equalsArgs(Statuses.USER_KEY),
                    Expression.greaterEqualsArgs(Statuses.SORT_ID)).sql
            val whereArgs = arrayOf(userId, upToStatusId)
            val values = ContentValues()
            values.putNull(Statuses.LOCATION)
            resolver.update(Statuses.CONTENT_URI, values, where, whereArgs)
            return true
        }

        override fun onStallWarning(warn: Warning): Boolean {
            return true
        }

        @Throws(IOException::class)
        override fun onStatus(status: Status): Boolean {
            return true
        }

        override fun onUnblock(source: User, unblockedUser: User): Boolean {
            val message = String.format("%s unblocked %s", source.screenName,
                    unblockedUser.screenName)
            Log.d(LOGTAG, message)
            return true
        }

        override fun onUnfavorite(source: User, target: User, targetStatus: Status): Boolean {
            val message = String.format("%s unfavorited %s's tweet: %s", source.screenName,
                    target.screenName, targetStatus.extendedText)
            Log.d(LOGTAG, message)
            return true
        }

    }

    companion object {

        private val NOTIFICATION_SERVICE_STARTED = 1

    }

}