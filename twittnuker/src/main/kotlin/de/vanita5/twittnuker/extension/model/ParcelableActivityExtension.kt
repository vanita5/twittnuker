/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.extension.model

import org.mariotaku.ktextension.addAllTo
import org.mariotaku.ktextension.isNullOrEmpty
import de.vanita5.twittnuker.model.ParcelableActivity
import java.util.*

val ParcelableActivity.id: String
    get() = "$min_position-$max_position"

val ParcelableActivity.reachedCountLimit: Boolean get() {
    fun Array<*>?.reachedCountLimit() = if (this == null) false else size > 10

    return sources.reachedCountLimit() || target_statuses.reachedCountLimit() ||
            target_users.reachedCountLimit() || target_user_lists.reachedCountLimit() ||
            target_object_statuses.reachedCountLimit() || target_object_users.reachedCountLimit() ||
            target_object_user_lists.reachedCountLimit()
}

fun ParcelableActivity.isSameSources(another: ParcelableActivity): Boolean {
    return Arrays.equals(sources, another.sources)
}

fun ParcelableActivity.isSameTarget(another: ParcelableActivity): Boolean {
    if (target_statuses.isNullOrEmpty() && target_users.isNullOrEmpty() && target_user_lists
            .isNullOrEmpty()) {
        return false
    }
    return Arrays.equals(target_users, another.target_users) && Arrays.equals(target_statuses,
            another.target_statuses) && Arrays.equals(target_user_lists, another.target_user_lists)
}

fun ParcelableActivity.isSameTargetObject(another: ParcelableActivity): Boolean {
    if (target_object_statuses.isNullOrEmpty() && target_object_users.isNullOrEmpty()
            && target_object_user_lists.isNullOrEmpty()) {
        return false
    }
    return Arrays.equals(target_object_users, another.target_object_users)
            && Arrays.equals(target_object_statuses, another.target_object_statuses)
            && Arrays.equals(target_object_user_lists, another.target_object_user_lists)
}

fun ParcelableActivity.prependSources(another: ParcelableActivity) {
    sources = uniqCombine(another.sources, sources)
}

fun ParcelableActivity.prependTargets(another: ParcelableActivity) {
    target_statuses = uniqCombine(another.target_statuses, target_statuses)
    target_users = uniqCombine(another.target_users, target_users)
    target_user_lists = uniqCombine(another.target_user_lists, target_user_lists)
}

fun ParcelableActivity.prependTargetObjects(another: ParcelableActivity) {
    target_object_statuses = uniqCombine(another.target_object_statuses, target_object_statuses)
    target_object_users = uniqCombine(another.target_object_users, target_object_users)
    target_object_user_lists = uniqCombine(another.target_object_user_lists, target_object_user_lists)
}

private inline fun <reified T> uniqCombine(vararg arrays: Array<T>?): Array<T> {
    val set = mutableSetOf<T>()
    arrays.forEach { array -> array?.addAllTo(set) }
    return set.toTypedArray()
}