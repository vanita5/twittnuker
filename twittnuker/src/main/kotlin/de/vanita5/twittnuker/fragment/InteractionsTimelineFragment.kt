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

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import de.vanita5.twittnuker.library.twitter.model.Activity
import org.mariotaku.sqliteqb.library.Expression
import de.vanita5.twittnuker.TwittnukerConstants.NOTIFICATION_ID_INTERACTIONS_TIMELINE
import de.vanita5.twittnuker.adapter.ParcelableActivitiesAdapter
import de.vanita5.twittnuker.annotation.ReadPositionTag
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_EXTRAS
import de.vanita5.twittnuker.model.ParameterizedExpression
import de.vanita5.twittnuker.model.RefreshTaskParam
import de.vanita5.twittnuker.model.tab.extra.InteractionsTabExtras
import de.vanita5.twittnuker.provider.TwidereDataStore.Activities
import de.vanita5.twittnuker.util.ErrorInfoStore

class InteractionsTimelineFragment : CursorActivitiesFragment() {

    override fun getActivities(param: RefreshTaskParam): Boolean {
        twitterWrapper.getActivitiesAboutMeAsync(param)
        return true
    }

    override val errorInfoKey: String
        get() = ErrorInfoStore.KEY_INTERACTIONS

    override val contentUri: Uri
        get() = Activities.AboutMe.CONTENT_URI

    override val notificationType: Int
        get() = NOTIFICATION_ID_INTERACTIONS_TIMELINE

    override val isFilterEnabled: Boolean
        get() = true

    override fun updateRefreshState() {
    }

    override fun processWhere(where: Expression, whereArgs: Array<String>): ParameterizedExpression {
        val arguments = arguments
        if (arguments != null) {
            val extras = arguments.getParcelable<InteractionsTabExtras>(EXTRA_EXTRAS)
            if (extras != null) {
                val expressions = mutableListOf(where)
                val combinedArgs = mutableListOf(*whereArgs)
                if (extras.isMentionsOnly) {
                    expressions.add(Expression.inArgs(Activities.ACTION, 3))
                    combinedArgs.addAll(arrayOf(Activity.Action.MENTION, Activity.Action.REPLY, Activity.Action.QUOTE))
                }
                if (extras.isMyFollowingOnly) {
                    expressions.add(Expression.equals(Activities.HAS_FOLLOWING_SOURCE, 1))
                }
                return ParameterizedExpression(Expression.and(*expressions.toTypedArray()),
                        combinedArgs.toTypedArray())
            }
        }
        return super.processWhere(where, whereArgs)
    }

    override fun onCreateAdapter(context: Context): ParcelableActivitiesAdapter {
        val adapter = ParcelableActivitiesAdapter(context, Glide.with(this))
        val arguments = arguments
        if (arguments != null) {
            val extras = arguments.getParcelable<InteractionsTabExtras>(EXTRA_EXTRAS)
            if (extras != null) {
                adapter.followingOnly = extras.isMyFollowingOnly
                adapter.mentionsOnly = extras.isMentionsOnly
            }
        }
        return adapter
    }

    @ReadPositionTag
    override val readPositionTag: String? = ReadPositionTag.ACTIVITIES_ABOUT_ME

}