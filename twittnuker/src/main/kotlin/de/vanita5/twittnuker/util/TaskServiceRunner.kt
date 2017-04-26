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

package de.vanita5.twittnuker.util

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.StringDef
import android.util.Log
import com.squareup.otto.Bus
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.kpreferences.get
import org.mariotaku.ktextension.mapToArray
import org.mariotaku.ktextension.toNulls
import de.vanita5.twittnuker.TwittnukerConstants.LOGTAG
import de.vanita5.twittnuker.constant.IntentConstants.INTENT_PACKAGE_PREFIX
import de.vanita5.twittnuker.constant.dataSyncProviderInfoKey
import de.vanita5.twittnuker.constant.stopAutoRefreshWhenBatteryLowKey
import de.vanita5.twittnuker.model.AccountPreferences
import de.vanita5.twittnuker.model.RefreshTaskParam
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.pagination.Pagination
import de.vanita5.twittnuker.model.pagination.SinceMaxPagination
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.task.filter.RefreshFiltersSubscriptionsTask
import de.vanita5.twittnuker.task.twitter.GetActivitiesAboutMeTask
import de.vanita5.twittnuker.task.twitter.GetHomeTimelineTask
import de.vanita5.twittnuker.task.twitter.message.GetMessagesTask


class TaskServiceRunner(
        val context: Context,
        val preferences: SharedPreferences,
        val bus: Bus
) {

    fun runTask(@Action action: String, callback: (Boolean) -> Unit): Boolean {
        Log.d(LOGTAG, "TaskServiceRunner run task $action")
        when (action) {
            ACTION_REFRESH_HOME_TIMELINE, ACTION_REFRESH_NOTIFICATIONS,
            ACTION_REFRESH_DIRECT_MESSAGES, ACTION_REFRESH_FILTERS_SUBSCRIPTIONS -> {
                val task = createRefreshTask(action) ?: return false
                task.callback = callback
                TaskStarter.execute(task)
                return true
            }
            ACTION_SYNC_DRAFTS, ACTION_SYNC_FILTERS, ACTION_SYNC_USER_COLORS -> {
                val runner = preferences[dataSyncProviderInfoKey]?.newSyncTaskRunner(context) ?: return false
                return runner.runTask(action, callback)
            }
        }
        return false
    }

    fun createRefreshTask(@Action action: String): AbstractTask<*, *, (Boolean) -> Unit>? {
        if (!Utils.isBatteryOkay(context) && preferences[stopAutoRefreshWhenBatteryLowKey]) {
            // Low battery, don't refresh
            return null
        }
        when (action) {
            ACTION_REFRESH_HOME_TIMELINE -> {
                val task = GetHomeTimelineTask(context)
                task.params = AutoRefreshTaskParam(context, preferences,
                        AccountPreferences::isAutoRefreshHomeTimelineEnabled) { accountKeys ->
                    DataStoreUtils.getNewestStatusIds(context, Statuses.CONTENT_URI, accountKeys.toNulls())
                }
                return task
            }
            ACTION_REFRESH_NOTIFICATIONS -> {
                val task = GetActivitiesAboutMeTask(context)
                task.params = AutoRefreshTaskParam(context, preferences,
                        AccountPreferences::isAutoRefreshMentionsEnabled) { accountKeys ->
                    DataStoreUtils.getRefreshNewestActivityMaxPositions(context,
                            Activities.AboutMe.CONTENT_URI, accountKeys.toNulls())
                }
                return task
            }
            ACTION_REFRESH_DIRECT_MESSAGES -> {
                val task = GetMessagesTask(context)
                    task.params = object : GetMessagesTask.RefreshNewTaskParam(context) {

                    override val isBackground: Boolean = true

                    override val accountKeys: Array<UserKey> by lazy {
                        AccountPreferences.getAccountPreferences(context, preferences,
                                DataStoreUtils.getAccountKeys(context)).filter {
                            it.isAutoRefreshEnabled && it.isAutoRefreshDirectMessagesEnabled
                        }.mapToArray(AccountPreferences::accountKey)
                    }
                }
                return task
            }
            ACTION_REFRESH_FILTERS_SUBSCRIPTIONS -> {
                return RefreshFiltersSubscriptionsTask(context)
            }
        }
        return null
    }

    @StringDef(ACTION_REFRESH_HOME_TIMELINE, ACTION_REFRESH_NOTIFICATIONS, ACTION_REFRESH_DIRECT_MESSAGES,
            ACTION_REFRESH_FILTERS_SUBSCRIPTIONS, ACTION_SYNC_DRAFTS, ACTION_SYNC_FILTERS,
            ACTION_SYNC_USER_COLORS)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Action

    class AutoRefreshTaskParam(
            val context: Context,
            val preferences: SharedPreferences,
            val refreshable: (AccountPreferences) -> Boolean,
            val getSinceIds: (Array<UserKey>) -> Array<String?>?
    ) : RefreshTaskParam {

        override val accountKeys: Array<UserKey> by lazy {
            return@lazy AccountPreferences.getAccountPreferences(context, preferences,
                    DataStoreUtils.getAccountKeys(context)).filter {
                it.isAutoRefreshEnabled && refreshable(it)
            }.mapToArray(AccountPreferences::accountKey)
        }

        override val pagination: Array<Pagination?>?
            get() = getSinceIds(accountKeys)?.mapToArray { sinceId ->
                SinceMaxPagination().also { it.sinceId = sinceId }
            }

        override val isBackground: Boolean = true
    }

    companion object {
        @Action
        const val ACTION_REFRESH_HOME_TIMELINE = INTENT_PACKAGE_PREFIX + "REFRESH_HOME_TIMELINE"
        @Action
        const val ACTION_REFRESH_NOTIFICATIONS = INTENT_PACKAGE_PREFIX + "REFRESH_NOTIFICATIONS"
        @Action
        const val ACTION_REFRESH_DIRECT_MESSAGES = INTENT_PACKAGE_PREFIX + "REFRESH_DIRECT_MESSAGES"
        @Action
        const val ACTION_REFRESH_FILTERS_SUBSCRIPTIONS = INTENT_PACKAGE_PREFIX + "REFRESH_FILTERS_SUBSCRIPTIONS"
        @Action
        const val ACTION_SYNC_DRAFTS = INTENT_PACKAGE_PREFIX + "SYNC_DRAFTS"
        @Action
        const val ACTION_SYNC_FILTERS = INTENT_PACKAGE_PREFIX + "SYNC_FILTERS"
        @Action
        const val ACTION_SYNC_USER_COLORS = INTENT_PACKAGE_PREFIX + "SYNC_USER_COLORS"

        val ACTIONS_SYNC = arrayOf(ACTION_SYNC_DRAFTS, ACTION_SYNC_FILTERS, ACTION_SYNC_USER_COLORS)
    }

    data class SyncFinishedEvent(val syncType: String, val success: Boolean)

}
