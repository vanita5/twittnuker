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
 *  GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.menu

import android.content.Context
import android.support.v4.view.ActionProvider
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ActionMenuView
import android.view.MenuItem
import de.vanita5.twittnuker.extension.view.findItemView

class RetweetItemProvider(context: Context) : ActionProvider(context) {
    var longClickListener: (() -> Boolean)? = null

    override fun onCreateActionView() = null

    fun init(menuBar: ActionMenuView, item: MenuItem) {
        assert(MenuItemCompat.getActionProvider(item) === this)
        val menuView = menuBar.findItemView(item)
        menuView?.setOnLongClickListener { longClickListener?.invoke() == true }
    }
}