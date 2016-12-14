/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import org.apache.commons.lang3.math.NumberUtils

import de.vanita5.twittnuker.BuildConfig
import de.vanita5.twittnuker.Constants
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.*
import de.vanita5.twittnuker.model.AccountPreferences
import de.vanita5.twittnuker.model.SimpleRefreshTaskParam
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.provider.TwidereDataStore.*
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.SharedPreferencesWrapper
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper
import javax.inject.Inject

class RefreshService : Service(), Constants {

    @Inject
    internal lateinit var preferences: SharedPreferencesWrapper
    @Inject
    internal lateinit var twitterWrapper: AsyncTwitterWrapper

    private lateinit var alarmManager: AlarmManager
    private var pendingRefreshHomeTimelineIntent: PendingIntent? = null
    private var pendingRefreshMentionsIntent: PendingIntent? = null
    private var pendingRefreshDirectMessagesIntent: PendingIntent? = null
    private var pendingRefreshTrendsIntent: PendingIntent? = null

    private val mStateReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BuildConfig.DEBUG) {
                Log.d(LOGTAG, String.format("Refresh service received action %s", action))
            }
            when (action) {
                BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING -> {
                    rescheduleHomeTimelineRefreshing()
                }
                BROADCAST_RESCHEDULE_MENTIONS_REFRESHING -> {
                    rescheduleMentionsRefreshing()
                }
                BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING -> {
                    rescheduleDirectMessagesRefreshing()
                }
                BROADCAST_RESCHEDULE_TRENDS_REFRESHING -> {
                    rescheduleTrendsRefreshing()
                }
                BROADCAST_REFRESH_HOME_TIMELINE -> {
                    if (isAutoRefreshAllowed) {
                        twitterWrapper.getHomeTimelineAsync(AutoRefreshTaskParam(context,
                                AccountPreferences::isAutoRefreshHomeTimelineEnabled) { accountKeys ->
                            DataStoreUtils.getNewestStatusIds(context, Statuses.CONTENT_URI, accountKeys)
                        })
                    }
                }
                BROADCAST_REFRESH_NOTIFICATIONS -> {
                    if (isAutoRefreshAllowed) {
                        twitterWrapper.getActivitiesAboutMeAsync(AutoRefreshTaskParam(context,
                                AccountPreferences::isAutoRefreshMentionsEnabled) { accountKeys ->
                            DataStoreUtils.getNewestActivityMaxPositions(context,
                                        Activities.AboutMe.CONTENT_URI, accountKeys)
                        })
                    }
                }
                BROADCAST_REFRESH_DIRECT_MESSAGES -> {
                    if (isAutoRefreshAllowed) {
                        twitterWrapper.getReceivedDirectMessagesAsync(AutoRefreshTaskParam(context,
                                AccountPreferences::isAutoRefreshDirectMessagesEnabled) { accountKeys ->
                            DataStoreUtils.getNewestMessageIds(context,
                                        DirectMessages.Inbox.CONTENT_URI, accountKeys)
                        })
                    }
                }
                BROADCAST_REFRESH_TRENDS -> {
                    if (isAutoRefreshAllowed) {
                        val prefs = AccountPreferences.getAccountPreferences(context,
                                DataStoreUtils.getAccountKeys(context)).filter(AccountPreferences::isAutoRefreshEnabled)
                        getLocalTrends(prefs.filter(AccountPreferences::isAutoRefreshTrendsEnabled)
                                .map(AccountPreferences::getAccountKey).toTypedArray())
                    }
                }
            }
        }

    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        GeneralComponentHelper.build(this).inject(this)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        pendingRefreshHomeTimelineIntent = PendingIntent.getBroadcast(this, 0, Intent(
                BROADCAST_REFRESH_HOME_TIMELINE), 0)
        pendingRefreshMentionsIntent = PendingIntent.getBroadcast(this, 0, Intent(BROADCAST_REFRESH_NOTIFICATIONS), 0)
        pendingRefreshDirectMessagesIntent = PendingIntent.getBroadcast(this, 0, Intent(
                BROADCAST_REFRESH_DIRECT_MESSAGES), 0)
        pendingRefreshTrendsIntent = PendingIntent.getBroadcast(this, 0, Intent(BROADCAST_REFRESH_TRENDS), 0)
        val refreshFilter = IntentFilter(BROADCAST_NOTIFICATION_DELETED)
        refreshFilter.addAction(BROADCAST_REFRESH_HOME_TIMELINE)
        refreshFilter.addAction(BROADCAST_REFRESH_NOTIFICATIONS)
        refreshFilter.addAction(BROADCAST_REFRESH_DIRECT_MESSAGES)
        refreshFilter.addAction(BROADCAST_RESCHEDULE_HOME_TIMELINE_REFRESHING)
        refreshFilter.addAction(BROADCAST_RESCHEDULE_MENTIONS_REFRESHING)
        refreshFilter.addAction(BROADCAST_RESCHEDULE_DIRECT_MESSAGES_REFRESHING)
        registerReceiver(mStateReceiver, refreshFilter)
        if (Utils.hasAutoRefreshAccounts(this)) {
            startAutoRefresh()
        } else {
            stopSelf()
        }
    }

    override fun onDestroy() {
        unregisterReceiver(mStateReceiver)
        if (Utils.hasAutoRefreshAccounts(this)) {
            // Auto refresh enabled, so I will try to start service after it was
            // stopped.
            startService(Intent(this, javaClass))
        }
        super.onDestroy()
    }

    protected val isAutoRefreshAllowed: Boolean
        get() = Utils.isNetworkAvailable(this) && (Utils.isBatteryOkay(this) || !Utils.shouldStopAutoRefreshOnBatteryLow(this))

    private fun getLocalTrends(accountIds: Array<UserKey>) {
        val account_id = Utils.getDefaultAccountKey(this)
        val woeid = preferences.getInt(KEY_LOCAL_TRENDS_WOEID, 1)
        twitterWrapper.getLocalTrendsAsync(account_id, woeid)
    }

    private val refreshInterval: Long
        get() {
            val prefValue = NumberUtils.toInt(preferences.getString(KEY_REFRESH_INTERVAL, DEFAULT_REFRESH_INTERVAL), -1)
            return (Math.max(prefValue, 3) * 60 * 1000).toLong()
        }

    private fun rescheduleDirectMessagesRefreshing() {
        alarmManager.cancel(pendingRefreshDirectMessagesIntent)
        val refreshInterval = refreshInterval
        if (refreshInterval > 0) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + refreshInterval,
                    refreshInterval, pendingRefreshDirectMessagesIntent)
        }
    }

    private fun rescheduleHomeTimelineRefreshing() {
        alarmManager.cancel(pendingRefreshHomeTimelineIntent)
        val refreshInterval = refreshInterval
        if (refreshInterval > 0) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + refreshInterval,
                    refreshInterval, pendingRefreshHomeTimelineIntent)
        }
    }

    private fun rescheduleMentionsRefreshing() {
        alarmManager.cancel(pendingRefreshMentionsIntent)
        val refreshInterval = refreshInterval
        if (refreshInterval > 0) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + refreshInterval,
                    refreshInterval, pendingRefreshMentionsIntent)
        }
    }

    private fun rescheduleTrendsRefreshing() {
        alarmManager.cancel(pendingRefreshTrendsIntent)
        val refreshInterval = refreshInterval
        if (refreshInterval > 0) {
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + refreshInterval,
                    refreshInterval, pendingRefreshTrendsIntent)
        }
    }

    private fun startAutoRefresh(): Boolean {
        stopAutoRefresh()
        val refreshInterval = refreshInterval
        if (refreshInterval <= 0) return false
        rescheduleHomeTimelineRefreshing()
        rescheduleMentionsRefreshing()
        rescheduleDirectMessagesRefreshing()
        rescheduleTrendsRefreshing()
        return true
    }

    private fun stopAutoRefresh() {
        alarmManager.cancel(pendingRefreshHomeTimelineIntent)
        alarmManager.cancel(pendingRefreshMentionsIntent)
        alarmManager.cancel(pendingRefreshDirectMessagesIntent)
        alarmManager.cancel(pendingRefreshTrendsIntent)
    }

    class AutoRefreshTaskParam(
            val context: Context,
            val refreshable: (AccountPreferences) -> Boolean,
            val getSinceIds: (Array<UserKey>) -> Array<String?>?
    ) : SimpleRefreshTaskParam() {

        override fun getAccountKeysWorker(): Array<UserKey> {
            val prefs = AccountPreferences.getAccountPreferences(context,
                    DataStoreUtils.getAccountKeys(context)).filter(AccountPreferences::isAutoRefreshEnabled)
            return prefs.filter(refreshable)
                    .map(AccountPreferences::getAccountKey).toTypedArray()
        }

        override val sinceIds: Array<String?>?
            get() = getSinceIds(accountKeys)
        }

}