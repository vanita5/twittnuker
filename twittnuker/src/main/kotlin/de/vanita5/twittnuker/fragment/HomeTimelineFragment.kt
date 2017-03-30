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

package de.vanita5.twittnuker.fragment

import android.net.Uri
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.TwittnukerConstants.NOTIFICATION_ID_HOME_TIMELINE
import de.vanita5.twittnuker.annotation.ReadPositionTag
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_EXTRAS
import de.vanita5.twittnuker.model.ParameterizedExpression
import de.vanita5.twittnuker.model.RefreshTaskParam
import de.vanita5.twittnuker.model.tab.extra.HomeTabExtras
import de.vanita5.twittnuker.provider.TwidereDataStore.Statuses
import de.vanita5.twittnuker.util.DataStoreUtils
import de.vanita5.twittnuker.util.ErrorInfoStore
import java.util.*

class HomeTimelineFragment : CursorStatusesFragment() {

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_HOME_TIMELINE

    override val contentUri: Uri
        get() = Statuses.CONTENT_URI

    override val notificationType: Int
        get() = NOTIFICATION_ID_HOME_TIMELINE

    override val isFilterEnabled: Boolean
        get() = true

    override fun updateRefreshState() {
        val twitter = twitterWrapper
        refreshing = twitter.isStatusTimelineRefreshing(contentUri)
    }

    override fun getStatuses(param: RefreshTaskParam): Boolean {
        if (!param.hasMaxIds) return twitterWrapper.refreshAll(param.accountKeys)
        return twitterWrapper.getHomeTimelineAsync(param)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        val context = context
        if (isVisibleToUser && context != null) {
            for (accountId in accountKeys) {
                val tag = "home_" + accountId
                notificationManager.cancel(tag, NOTIFICATION_ID_HOME_TIMELINE)
            }
        }
    }

    override fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        val arguments = arguments
        if (arguments != null) {
            val extras = arguments.getParcelable<HomeTabExtras>(EXTRA_EXTRAS)
            if (extras != null) {
                val expressions = ArrayList<Expression>()
                val expressionArgs = ArrayList<String>()
                Collections.addAll(expressionArgs, *whereArgs)
                expressions.add(where)
                DataStoreUtils.processTabExtras(expressions, expressionArgs, extras)
                val expression = Expression.and(*expressions.toTypedArray())
                return ParameterizedExpression(expression, expressionArgs.toTypedArray())
            }
        }
        return super.processWhere(where, whereArgs)
    }

    override val readPositionTag: String = ReadPositionTag.HOME_TIMELINE

}