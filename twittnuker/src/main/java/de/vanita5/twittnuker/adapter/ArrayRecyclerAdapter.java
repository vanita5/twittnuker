/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2016 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2016 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class ArrayRecyclerAdapter<T, H extends ViewHolder> extends BaseRecyclerViewAdapter<H> {

    protected final ArrayList<T> mData = new ArrayList<>();

    public ArrayRecyclerAdapter(Context context) {
        super(context);
    }

    @Override
    public final void onBindViewHolder(H holder, int position) {
        onBindViewHolder(holder, position, getItem(position));
    }

    public abstract void onBindViewHolder(H holder, int position, T item);


    public void add(final T item) {
        if (item == null) return;
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addAll(final Collection<? extends T> collection) {
        mData.addAll(collection);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public T getItem(final int position) {
        return mData.get(position);
    }

    public boolean remove(final int position) {
        mData.remove(position);
        notifyItemRemoved(position);
        return true;
    }

    public void removeAll(final List<T> collection) {
        mData.removeAll(collection);
        notifyDataSetChanged();
    }

    public void sort(final Comparator<? super T> comparator) {
        Collections.sort(mData, comparator);
        notifyDataSetChanged();
    }
}