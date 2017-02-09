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

import android.support.v4.view.PagerAdapter
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup

abstract class RecyclerPagerAdapter : PagerAdapter() {
    private val viewHolders: SparseArray<ViewHolder> = SparseArray()

    final override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemViewType = getItemViewType(position)
        val holder = onCreateViewHolder(container, position, itemViewType)
        holder.position = position
        holder.itemViewType = itemViewType
        viewHolders.put(position, holder)
        container.addView(holder.itemView)
        onBindViewHolder(holder, position, holder.itemViewType)
        return holder
    }

    final override fun destroyItem(container: ViewGroup, position: Int, obj: Any?) {
        val holder = obj as ViewHolder
        viewHolders.remove(position)
        container.removeView(holder.itemView)
    }

    final override fun getItemPosition(obj: Any?): Int {
        for (i in 0 until viewHolders.size()) {
            val position = viewHolders.keyAt(i)
            val holder = viewHolders.valueAt(i)
            if (holder === obj) {
                onBindViewHolder(holder, position, holder.itemViewType)
                return POSITION_UNCHANGED
            }
        }
        return POSITION_NONE
    }

    final override fun isViewFromObject(view: View, obj: Any): Boolean {
        return (obj as ViewHolder).itemView == view
    }

    abstract fun onCreateViewHolder(container: ViewGroup, position: Int, itemViewType: Int): ViewHolder

    abstract fun onBindViewHolder(holder: ViewHolder, position: Int, itemViewType: Int)

    open fun getItemViewType(position: Int): Int = 0

    fun notifyPagesChanged(invalidateCache: Boolean = true) {
        if (invalidateCache) {
            viewHolders.clear()
        }
        super.notifyDataSetChanged()
    }

    abstract class ViewHolder(val itemView: View) {
        var position: Int = NO_POSITION
        var itemViewType: Int = 0
    }

    companion object {
        const val NO_POSITION = -1
    }
}