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
import de.vanita5.twittnuker.annotation.AccountType
import de.vanita5.twittnuker.annotation.TabAccountFlags
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER_LIST
import de.vanita5.twittnuker.fragment.statuses.UserListTimelineFragment
import de.vanita5.twittnuker.model.AccountDetails
import de.vanita5.twittnuker.model.Tab
import de.vanita5.twittnuker.model.tab.DrawableHolder
import de.vanita5.twittnuker.model.tab.StringHolder
import de.vanita5.twittnuker.model.tab.TabConfiguration
import de.vanita5.twittnuker.model.tab.argument.UserListArguments
import de.vanita5.twittnuker.model.tab.conf.UserListExtraConfiguration

class UserListTimelineTabConfiguration : TabConfiguration() {

    override val name = StringHolder.resource(R.string.list_timeline)

    override val icon = DrawableHolder.Builtin.LIST

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or
            TabAccountFlags.FLAG_ACCOUNT_REQUIRED

    override val fragmentClass = UserListTimelineFragment::class.java

    override fun checkAccountAvailability(details: AccountDetails) = when (details.type) {
        AccountType.TWITTER -> true
        else -> false
    }

    override fun getExtraConfigurations(context: Context) = arrayOf(
            UserListExtraConfiguration(EXTRA_USER_LIST).headerTitle(R.string.title_user_list)
    )

    override fun applyExtraConfigurationTo(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val arguments = tab.arguments as UserListArguments
        when (extraConf.key) {
            EXTRA_USER_LIST -> {
                val userList = (extraConf as UserListExtraConfiguration).value ?: return false
                arguments.listId = userList.id
            }
        }
        return true
    }
}