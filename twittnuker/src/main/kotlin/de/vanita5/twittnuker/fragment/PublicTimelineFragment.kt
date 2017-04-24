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
import android.os.Bundle
import android.support.v4.content.Loader
import de.vanita5.twittnuker.TwittnukerConstants.*
import de.vanita5.twittnuker.loader.statuses.PublicTimelineLoader
import de.vanita5.twittnuker.model.ParcelableStatus
import de.vanita5.twittnuker.util.Utils
import java.util.*

class PublicTimelineFragment : ParcelableStatusesFragment() {

    override fun onCreateStatusesLoader(context: Context, args: Bundle,
                                        fromUser: Boolean): Loader<List<ParcelableStatus>?> {
        refreshing = true
        val data = adapterData
        val accountKey = Utils.getAccountKey(context, args)
        val maxId = args.getString(EXTRA_MAX_ID)
        val sinceId = args.getString(EXTRA_SINCE_ID)
        val tabPosition = args.getInt(EXTRA_TAB_POSITION, -1)
        val loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false)
        return PublicTimelineLoader(context, accountKey, sinceId, maxId, data,
                savedStatusesFileArgs, tabPosition, fromUser, loadingMore)
    }

    override val savedStatusesFileArgs: Array<String>?
        get() {
            val accountKey = Utils.getAccountKey(context, arguments)
            val result = ArrayList<String>()
            result.add(AUTHORITY_PUBLIC_TIMELINE)
            result.add("account=$accountKey")
            return result.toTypedArray()
        }

    fun onLoadMoreContents(position: Int) {
        // Ignore for now cause Fanfou doesn't support load more for public timeline.
    }

    override val readPositionTagWithArguments: String?
        get() {
            val tabPosition = arguments.getInt(EXTRA_TAB_POSITION, -1)
            if (tabPosition < 0) return null
            return "public_timeline"
        }
}