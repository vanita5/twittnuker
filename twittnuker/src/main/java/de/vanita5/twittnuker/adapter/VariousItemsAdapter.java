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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.ParcelableUserList;
import de.vanita5.twittnuker.util.StatusAdapterLinkClickHandler;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.TwidereLinkify;
import de.vanita5.twittnuker.view.holder.StatusViewHolder;
import de.vanita5.twittnuker.view.holder.UserListViewHolder;
import de.vanita5.twittnuker.view.holder.UserViewHolder;

import java.util.List;

public class VariousItemsAdapter extends LoadMoreSupportAdapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_STATUS = 1;
    public static final int VIEW_TYPE_USER = 2;
    public static final int VIEW_TYPE_USER_LIST = 3;

    private final boolean mCompact;
    private final LayoutInflater mInflater;
    private final int mCardBackgroundColor;
    private final DummyItemAdapter mDummyAdapter;

    private List<?> mData;

    public VariousItemsAdapter(Context context, boolean compact) {
        super(context);
        mCompact = compact;
        mInflater = LayoutInflater.from(context);
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                ThemeUtils.getThemeBackgroundOption(context),
                ThemeUtils.getUserThemeBackgroundAlpha(context));
        final StatusAdapterLinkClickHandler<Object> handler = new StatusAdapterLinkClickHandler<>(context,
                mPreferences);
        mDummyAdapter = new DummyItemAdapter(context, new TwidereLinkify(handler), this);
        handler.setAdapter(mDummyAdapter);
        mDummyAdapter.updateOptions();
        setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_STATUS: {
                return ListParcelableStatusesAdapter.createStatusViewHolder(mDummyAdapter,
                        mInflater, parent, mCompact, mCardBackgroundColor);
            }
            case VIEW_TYPE_USER: {
                return ParcelableUsersAdapter.createUserViewHolder(mDummyAdapter, mInflater, parent,
                        mCardBackgroundColor);
            }
            case VIEW_TYPE_USER_LIST: {
                return ParcelableUserListsAdapter.createUserListViewHolder(mDummyAdapter, mInflater,
                        parent, mCardBackgroundColor);
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Object obj = mData.get(position);
        switch (getItemViewType(obj)) {
            case VIEW_TYPE_STATUS: {
                ((StatusViewHolder) holder).displayStatus((ParcelableStatus) obj, true);
                break;
            }
            case VIEW_TYPE_USER: {
                ((UserViewHolder) holder).displayUser((ParcelableUser) obj);
                break;
            }
            case VIEW_TYPE_USER_LIST: {
                ((UserListViewHolder) holder).displayUserList((ParcelableUserList) obj);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = mData.get(position);
        return getItemViewType(obj);
    }

    protected int getItemViewType(Object obj) {
        if (obj instanceof ParcelableStatus) {
            return VIEW_TYPE_STATUS;
        } else if (obj instanceof ParcelableUser) {
            return VIEW_TYPE_USER;
        } else if (obj instanceof ParcelableUserList) {
            return VIEW_TYPE_USER_LIST;
        }
        throw new UnsupportedOperationException("Unsupported object " + obj);
    }

    public void setData(List<?> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public DummyItemAdapter getDummyAdapter() {
        return mDummyAdapter;
    }
}