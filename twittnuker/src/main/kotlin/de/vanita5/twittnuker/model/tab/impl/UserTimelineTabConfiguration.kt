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
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_USER
import de.vanita5.twittnuker.fragment.statuses.UserTimelineFragment
import de.vanita5.twittnuker.model.Tab
import de.vanita5.twittnuker.model.tab.DrawableHolder
import de.vanita5.twittnuker.model.tab.StringHolder
import de.vanita5.twittnuker.model.tab.TabConfiguration
import de.vanita5.twittnuker.model.tab.argument.UserArguments
import de.vanita5.twittnuker.model.tab.conf.UserExtraConfiguration

class UserTimelineTabConfiguration : TabConfiguration() {

    override val name = StringHolder.resource(R.string.users_statuses)

    override val icon = DrawableHolder.Builtin.USER

    override val accountFlags = TabAccountFlags.FLAG_HAS_ACCOUNT or TabAccountFlags.FLAG_ACCOUNT_REQUIRED

    override val fragmentClass = UserTimelineFragment::class.java

    override fun getExtraConfigurations(context: Context) = arrayOf(
            UserExtraConfiguration(EXTRA_USER).headerTitle(R.string.title_user)
    )

    override fun applyExtraConfigurationTo(tab: Tab, extraConf: TabConfiguration.ExtraConfiguration): Boolean {
        val arguments = tab.arguments as UserArguments
        when (extraConf.key) {
            EXTRA_USER -> {
                val user = (extraConf as UserExtraConfiguration).value ?: return false
                arguments.setUserKey(user.key)
            }
        }
        return true
    }
}