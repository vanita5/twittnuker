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

package de.vanita5.twittnuker.model

import android.os.Bundle
import android.support.v4.app.Fragment
import de.vanita5.twittnuker.annotation.CustomTabType
import de.vanita5.twittnuker.model.tab.DrawableHolder
import de.vanita5.twittnuker.util.CompareUtils.bundleEquals
import de.vanita5.twittnuker.util.CompareUtils.objectEquals

data class SupportTabSpec(
        var name: CharSequence? = null,
        var icon: DrawableHolder? = null,
        @CustomTabType val type: String? = null,
        var cls: Class<out Fragment>,
        var args: Bundle? = null,
        var position: Int,
        var tag: String? = null
) : Comparable<SupportTabSpec> {

    init {
        if (name == null && icon == null)
            throw IllegalArgumentException("You must specify a name or icon for this tab!")
    }

    override fun compareTo(other: SupportTabSpec): Int {
        return position - other.position
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SupportTabSpec) return false
        return objectEquals(name, other.name) && objectEquals(icon, other.icon) && cls == other.cls
                && bundleEquals(args, other.args) && position == other.position
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + cls.hashCode()
        result = 31 * result + (args?.hashCode() ?: 0)
        result = 31 * result + position
        return result
    }

}