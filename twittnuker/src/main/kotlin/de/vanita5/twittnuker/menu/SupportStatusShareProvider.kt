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

package de.vanita5.twittnuker.menu

import android.content.Context
import android.support.v4.view.ActionProvider
import android.view.MenuItem
import android.view.SubMenu
import de.vanita5.twittnuker.Constants.MENU_GROUP_STATUS_SHARE
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.util.MenuUtils
import de.vanita5.twittnuker.util.Utils.createStatusShareIntent

class SupportStatusShareProvider(context: Context) : ActionProvider(context) {
    var status: ParcelableStatus? = null

    override fun onCreateActionView() = null

    override fun onCreateActionView(forItem: MenuItem) = null

    override fun onPerformDefaultAction() = true

    override fun hasSubMenu() = true

    override fun onPrepareSubMenu(subMenu: SubMenu) {
        val status = status ?: return
        val shareIntent = createStatusShareIntent(context, status)
        subMenu.removeGroup(MENU_GROUP_STATUS_SHARE)
        MenuUtils.addIntentToMenu(context, subMenu, shareIntent, MENU_GROUP_STATUS_SHARE)
    }

}