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

package de.vanita5.twittnuker.model.tab.impl

import android.content.Context
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.annotation.TabAccountFlags
import de.vanita5.twittnuker.constant.IntentConstants.*
import de.vanita5.twittnuker.fragment.HomeTimelineFragment
import de.vanita5.twittnuker.model.Tab
import de.vanita5.twittnuker.model.tab.DrawableHolder
import de.vanita5.twittnuker.model.tab.StringHolder
import de.vanita5.twittnuker.model.tab.TabConfiguration
import de.vanita5.twittnuker.model.tab.conf.BooleanExtraConfiguration
import de.vanita5.twittnuker.model.tab.extra.HomeTabExtras


class HomeTabConfiguration : TabConfiguration() {

    override val name = StringHolder.resource(R.string.title_home)

    override val icon = DrawableHolder.Builtin.HOME

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or
            TabAccountFlags.FLAG_ACCOUNT_MULTIPLE or TabAccountFlags.FLAG_ACCOUNT_MUTABLE

    override val fragmentClass = HomeTimelineFragment::class.java

    override fun getExtraConfigurations(context: Context) = arrayOf(
            BooleanExtraConfiguration(EXTRA_HIDE_RETWEETS, R.string.hide_retweets, false).mutable(true),
            BooleanExtraConfiguration(EXTRA_HIDE_QUOTES, R.string.hide_quotes, false).mutable(true),
            BooleanExtraConfiguration(EXTRA_HIDE_REPLIES, R.string.hide_replies, false).mutable(true)
    )

    override fun applyExtraConfigurationTo(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val extras = tab.extras as HomeTabExtras
        when (extraConf.key) {
            EXTRA_HIDE_RETWEETS -> {
                extras.isHideRetweets = (extraConf as BooleanExtraConfiguration).value
            }
            EXTRA_HIDE_QUOTES -> {
                extras.isHideQuotes = (extraConf as BooleanExtraConfiguration).value
            }
            EXTRA_HIDE_REPLIES -> {
                extras.isHideReplies = (extraConf as BooleanExtraConfiguration).value
            }
        }
        return true
    }

    override fun readExtraConfigurationFrom(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val extras = tab.extras as? HomeTabExtras ?: return false
        when (extraConf.key) {
            EXTRA_HIDE_RETWEETS -> {
                (extraConf as BooleanExtraConfiguration).value = extras.isHideRetweets
            }
            EXTRA_HIDE_QUOTES -> {
                (extraConf as BooleanExtraConfiguration).value = extras.isHideQuotes
            }
            EXTRA_HIDE_REPLIES -> {
                (extraConf as BooleanExtraConfiguration).value = extras.isHideReplies
            }
        }
        return true
    }
}