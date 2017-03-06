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

package de.vanita5.twittnuker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.iface.IStatusesAdapter
import de.vanita5.twittnuker.view.holder.StatusViewHolder
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder

class ListParcelableStatusesAdapter(
        context: Context,
        requestManager: RequestManager
) : ParcelableStatusesAdapter(context, requestManager) {

    override val progressViewIds: IntArray
        get() = intArrayOf(R.id.media_preview_progress)

    override fun onCreateStatusViewHolder(parent: ViewGroup): IStatusViewHolder {
        return createStatusViewHolder(this, inflater, parent)
    }

    companion object {

        fun createStatusViewHolder(adapter: IStatusesAdapter<*>,
                                   inflater: LayoutInflater, parent: ViewGroup): StatusViewHolder {
            val view = inflater.inflate(StatusViewHolder.layoutResource, parent, false)
            val holder = StatusViewHolder(adapter, view)
            holder.setOnClickListeners()
            holder.setupViewOptions()
            return holder
        }
    }
}