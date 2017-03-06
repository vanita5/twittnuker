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
import android.support.v7.widget.RecyclerView.ViewHolder
import com.bumptech.glide.RequestManager
import java.util.*

abstract class ArrayRecyclerAdapter<T, H : ViewHolder>(
        context: Context,
        requestManager: RequestManager
) : BaseRecyclerViewAdapter<H>(context, requestManager) {

    protected val data = ArrayList<T>()

    override fun onBindViewHolder(holder: H, position: Int) {
        onBindViewHolder(holder, position, getItem(position))
    }

    abstract fun onBindViewHolder(holder: H, position: Int, item: T)


    fun add(item: T?) {
        if (item == null) return
        data.add(item)
        notifyDataSetChanged()
    }

    fun addAll(collection: Collection<T>) {
        data.addAll(collection)
        notifyDataSetChanged()
    }

    fun clear() {
        data.clear()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getItem(position: Int): T {
        return data[position]
    }

    fun remove(position: Int): Boolean {
        data.removeAt(position)
        notifyItemRemoved(position)
        return true
    }

    fun removeAll(collection: List<T>) {
        data.removeAll(collection)
        notifyDataSetChanged()
    }

    fun sort(comparator: Comparator<in T>) {
        Collections.sort(data, comparator)
        notifyDataSetChanged()
    }
}