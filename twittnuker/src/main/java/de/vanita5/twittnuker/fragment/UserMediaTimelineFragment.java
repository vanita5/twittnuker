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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.commons.lang3.ArrayUtils;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.ParcelableStatusesAdapter;
import de.vanita5.twittnuker.adapter.StaggeredGridParcelableStatusesAdapter;
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter.IndicatorPosition;
import de.vanita5.twittnuker.adapter.iface.IStatusesAdapter;
import de.vanita5.twittnuker.loader.MediaTimelineLoader;
import de.vanita5.twittnuker.loader.iface.IExtendedLoader;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.util.IntentUtils;
import de.vanita5.twittnuker.view.HeaderDrawerLayout.DrawerCallback;
import de.vanita5.twittnuker.view.holder.GapViewHolder;
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder;

import java.util.List;

public class UserMediaTimelineFragment extends AbsContentRecyclerViewFragment<StaggeredGridParcelableStatusesAdapter, StaggeredGridLayoutManager>
        implements LoaderCallbacks<List<ParcelableStatus>>, DrawerCallback, IStatusViewHolder.StatusClickListener {


    @Override
    protected void scrollToPositionWithOffset(int position, int offset) {
        getLayoutManager().scrollToPositionWithOffset(position, offset);
    }


    @Override
    public boolean isRefreshing() {
        if (getContext() == null || isDetached()) return false;
        return getLoaderManager().hasRunningLoaders();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ParcelableStatusesAdapter adapter = getAdapter();
        adapter.setStatusClickListener(this);
        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
        showProgress();
    }

    @Override
    protected void setupRecyclerView(Context context, boolean compact) {

    }

    @NonNull
    @Override
    protected StaggeredGridLayoutManager onCreateLayoutManager(Context context) {
        return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }


    public int getStatuses(final String maxId, final String sinceId) {
        if (getContext() == null) return -1;
        final Bundle args = new Bundle(getArguments());
        args.putBoolean(EXTRA_MAKE_GAP, false);
        args.putString(EXTRA_MAX_ID, maxId);
        args.putString(EXTRA_SINCE_ID, sinceId);
        args.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().restartLoader(0, args, this);
        return 0;
    }


    @NonNull
    @Override
    protected StaggeredGridParcelableStatusesAdapter onCreateAdapter(Context context, boolean compact) {
        return new StaggeredGridParcelableStatusesAdapter(context, compact);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_recyclerview, container, false);
    }

    @Override
    public Loader<List<ParcelableStatus>> onCreateLoader(int id, Bundle args) {
        final Context context = getActivity();
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String maxId = args.getString(EXTRA_MAX_ID);
        final String sinceId = args.getString(EXTRA_SINCE_ID);
        final String userId = args.getString(EXTRA_USER_ID);
        final String screenName = args.getString(EXTRA_SCREEN_NAME);
        final int tabPosition = args.getInt(EXTRA_TAB_POSITION, -1);
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        final boolean loadingMore = args.getBoolean(EXTRA_LOADING_MORE, false);
        return new MediaTimelineLoader(context, accountKey, userId, screenName, sinceId, maxId,
                getAdapter().getData(), null, tabPosition, fromUser, loadingMore);
    }

    @Override
    public void onLoadFinished(Loader<List<ParcelableStatus>> loader, List<ParcelableStatus> data) {
        final StaggeredGridParcelableStatusesAdapter adapter = getAdapter();
        adapter.setData(data);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupportedPosition(hasMoreData(data) ? IndicatorPosition.END : IndicatorPosition.NONE);
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        showContent();
        setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
    }

    private boolean hasMoreData(List<ParcelableStatus> data) {
        return true;
    }

    @Override
    public void onLoaderReset(Loader<List<ParcelableStatus>> loader) {
        getAdapter().setData(null);
    }

    @Override
    public boolean isReachingEnd() {
        final StaggeredGridLayoutManager lm = getLayoutManager();
        return ArrayUtils.contains(lm.findLastCompletelyVisibleItemPositions(null), lm.getItemCount() - 1);
    }

    @Override
    public boolean isReachingStart() {
        final StaggeredGridLayoutManager lm = getLayoutManager();
        return ArrayUtils.contains(lm.findFirstCompletelyVisibleItemPositions(null), 0);
    }

    @Override
    public void onLoadMoreContents(int position) {
        // Only supports load from end, skip START flag
        if ((position & IndicatorPosition.START) != 0) return;
        super.onLoadMoreContents(position);
        if (position == 0) return;
        final IStatusesAdapter<List<ParcelableStatus>> adapter = getAdapter();
        final String maxId = adapter.getStatusId(adapter.getStatusCount() - 1);
        getStatuses(maxId, null);
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {

    }

    @Override
    public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int statusPosition) {

    }

    @Override
    public void onItemActionClick(RecyclerView.ViewHolder holder, int id, int position) {

    }

    @Override
    public void onStatusClick(IStatusViewHolder holder, int position) {
        IntentUtils.openStatus(getContext(), getAdapter().getStatus(position), null);
    }

    @Override
    public boolean onStatusLongClick(IStatusViewHolder holder, int position) {
        return false;
    }

    @Override
    public void onItemMenuClick(RecyclerView.ViewHolder holder, View menuView, int position) {

    }

    @Override
    public void onUserProfileClick(IStatusViewHolder holder, int position) {

    }
}