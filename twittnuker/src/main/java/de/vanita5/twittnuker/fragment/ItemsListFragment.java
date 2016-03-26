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

package de.vanita5.twittnuker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;

import de.vanita5.twittnuker.adapter.DummyItemAdapter;
import de.vanita5.twittnuker.adapter.VariousItemsAdapter;
import de.vanita5.twittnuker.adapter.decorator.DividerItemDecoration;
import de.vanita5.twittnuker.adapter.iface.IUsersAdapter;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.util.IntentUtils;
import de.vanita5.twittnuker.view.holder.UserViewHolder;
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder;

import java.util.List;

public class ItemsListFragment extends AbsContentListRecyclerViewFragment<VariousItemsAdapter>
        implements LoaderCallbacks<List<?>> {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
        setRefreshEnabled(false);
        showContent();
    }

    @NonNull
    @Override
    protected VariousItemsAdapter onCreateAdapter(Context context, boolean compact) {
        final VariousItemsAdapter adapter = new VariousItemsAdapter(context, compact);
        final DummyItemAdapter dummyItemAdapter = adapter.getDummyAdapter();
        dummyItemAdapter.setStatusClickListener(new IStatusViewHolder.SimpleStatusClickListener() {
            @Override
            public void onStatusClick(IStatusViewHolder holder, int position) {
                final ParcelableStatus status = dummyItemAdapter.getStatus(position);
                if (status == null) return;
                IntentUtils.openStatus(getContext(), status, null);
            }
        });
        dummyItemAdapter.setUserClickListener(new IUsersAdapter.SimpleUserClickListener() {
            @Override
            public void onUserClick(UserViewHolder holder, int position) {
                final ParcelableUser user = dummyItemAdapter.getUser(position);
                if (user == null) return;
                IntentUtils.openUserProfile(getContext(), user, null,
                        mPreferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        UserFragment.Referral.TIMELINE_STATUS);
            }
        });
        return adapter;
    }

    @Override
    public Loader<List<?>> onCreateLoader(int id, Bundle args) {
        return new ItemsLoader(getContext(), getArguments());
    }

    @Override
    public void onLoadFinished(Loader<List<?>> loader, List<?> data) {
        getAdapter().setData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<?>> loader) {
        getAdapter().setData(null);
    }

    @Override
    protected void setupRecyclerView(Context context, boolean compact) {
        if (compact) {
            super.setupRecyclerView(context, true);
            return;
        }
        final RecyclerView recyclerView = getRecyclerView();
        final VariousItemsAdapter adapter = getAdapter();
        // Dividers are drawn on bottom of view
        recyclerView.addItemDecoration(new DividerItemDecoration(context, getLayoutManager().getOrientation()) {

            @Override
            protected boolean isDividerEnabled(int childPos) {
                // Don't draw for last item
                if (childPos == RecyclerView.NO_POSITION || childPos == adapter.getItemCount() - 1) {
                    return false;
                }
                final int itemViewType = adapter.getItemViewType(childPos);
                // Draw only if current item and next item is TITLE_SUMMARY
                if (shouldUseDividerFor(itemViewType)) {
                    if (shouldUseDividerFor(adapter.getItemViewType(childPos + 1))) {
                        return true;
                    }
                }
                return false;
            }

            private boolean shouldUseDividerFor(int itemViewType) {
                switch (itemViewType) {
                    case VariousItemsAdapter.VIEW_TYPE_USER:
                    case VariousItemsAdapter.VIEW_TYPE_USER_LIST:
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

    public static class ItemsLoader extends AsyncTaskLoader<List<?>> {
        private final Bundle mArguments;

        public ItemsLoader(Context context, Bundle args) {
            super(context);
            mArguments = args;
        }

        @Override
        public List<?> loadInBackground() {
            return mArguments.<Parcelable>getParcelableArrayList(EXTRA_ITEMS);
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }
}