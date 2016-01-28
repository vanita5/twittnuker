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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.KeyEvent;
import android.view.View;

import com.squareup.otto.Subscribe;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.AbsActivitiesAdapter;
import de.vanita5.twittnuker.adapter.decorator.DividerItemDecoration;
import de.vanita5.twittnuker.annotation.ReadPositionTag;
import de.vanita5.twittnuker.loader.iface.IExtendedLoader;
import de.vanita5.twittnuker.model.ParcelableActivity;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.util.ParcelableActivityUtils;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import de.vanita5.twittnuker.util.RecyclerViewNavigationHelper;
import de.vanita5.twittnuker.util.RecyclerViewUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.imageloader.PauseRecyclerViewOnScrollListener;
import de.vanita5.twittnuker.util.message.StatusListChangedEvent;
import de.vanita5.twittnuker.view.holder.ActivityTitleSummaryViewHolder;
import de.vanita5.twittnuker.view.holder.GapViewHolder;
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder;

import java.util.Arrays;

public abstract class AbsActivitiesFragment<Data> extends AbsContentListRecyclerViewFragment<AbsActivitiesAdapter<Data>>
        implements LoaderCallbacks<Data>, AbsActivitiesAdapter.ActivityAdapterListener, KeyboardShortcutCallback {

    private final Object mStatusesBusCallback;
    private PopupMenu mPopupMenu;
    private final OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                final LinearLayoutManager layoutManager = getLayoutManager();
                saveReadPosition(layoutManager.findFirstVisibleItemPosition());
            }
        }
    };
    private RecyclerViewNavigationHelper mNavigationHelper;
    private OnScrollListener mPauseOnScrollListener;

    protected AbsActivitiesFragment() {
        mStatusesBusCallback = createMessageBusCallback();
    }

    public abstract boolean getActivities(long[] accountIds, long[] maxIds, long[] sinceIds);

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_REFRESH.equals(action)) {
            triggerRefresh();
            return true;
        }
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        if (recyclerView == null || layoutManager == null) return false;
        final View focusedChild = RecyclerViewUtils.findRecyclerViewChild(recyclerView,
                layoutManager.getFocusedChild());
        int position = -1;
        if (focusedChild != null && focusedChild.getParent() == recyclerView) {
            position = recyclerView.getChildLayoutPosition(focusedChild);
        }
        if (position != -1) {
            final ParcelableActivity activity = getAdapter().getActivity(position);
            if (activity == null) return false;
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                openActivity(activity);
                return true;
            }
            final ParcelableStatus status = ParcelableActivity.getActivityStatus(activity);
            if (status == null) return false;
            if (action == null) {
                action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState);
            }
            if (action == null) return false;
            switch (action) {
                case ACTION_STATUS_REPLY: {
                    final Intent intent = new Intent(INTENT_ACTION_REPLY);
                    intent.putExtra(EXTRA_STATUS, status);
                    startActivity(intent);
                    return true;
                }
                case ACTION_STATUS_RETWEET: {
                    RetweetQuoteDialogFragment.show(getFragmentManager(), status);
                    return true;
                }
                case ACTION_STATUS_FAVORITE: {
                    final AsyncTwitterWrapper twitter = mTwitterWrapper;
                    if (status.is_favorite) {
                        twitter.destroyFavoriteAsync(activity.account_id, status.id);
                    } else {
                        twitter.createFavoriteAsync(activity.account_id, status.id);
                    }
                    return true;
                }
            }
        }
        return mNavigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    private void openActivity(ParcelableActivity activity) {
        final ParcelableStatus status = ParcelableActivity.getActivityStatus(activity);
        if (status != null) {
            Utils.openStatus(getContext(), status, null);
        } else {

        }
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_REFRESH.equals(action)) {
            return true;
        }
        if (action == null) {
            action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState);
        }
        if (action == null) return false;
        switch (action) {
            case ACTION_STATUS_REPLY:
            case ACTION_STATUS_RETWEET:
            case ACTION_STATUS_FAVORITE:
                return true;
        }
        return mNavigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public final Loader<Data> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateStatusesLoader(getActivity(), args, fromUser);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            final LinearLayoutManager layoutManager = getLayoutManager();
            if (layoutManager != null) {
                saveReadPosition(layoutManager.findFirstVisibleItemPosition());
            }
        }
    }

    @Override
    public final void onLoadFinished(Loader<Data> loader, Data data) {
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        final boolean rememberPosition = mPreferences.getBoolean(KEY_REMEMBER_POSITION, false);
        final boolean readFromBottom = mPreferences.getBoolean(KEY_READ_FROM_BOTTOM, false);
        final long lastReadId;
        final int lastVisiblePos, lastVisibleTop;
        final String tag = getCurrentReadPositionTag();
        final LinearLayoutManager layoutManager = getLayoutManager();
        if (readFromBottom) {
            lastVisiblePos = layoutManager.findLastVisibleItemPosition();
        } else {
            lastVisiblePos = layoutManager.findFirstVisibleItemPosition();
        }
        if (lastVisiblePos != RecyclerView.NO_POSITION && lastVisiblePos < adapter.getItemCount()) {
            lastReadId = adapter.getTimestamp(lastVisiblePos);
            final View positionView = layoutManager.findViewByPosition(lastVisiblePos);
            lastVisibleTop = positionView != null ? positionView.getTop() : 0;
        } else if (rememberPosition && tag != null) {
            lastReadId = mReadStateManager.getPosition(tag);
            lastVisibleTop = 0;
        } else {
            lastReadId = -1;
            lastVisibleTop = 0;
        }
        adapter.setData(data);
        setRefreshEnabled(true);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupported(hasMoreData(data));
            int pos = -1;
            for (int i = 0, j = adapter.getItemCount(); i < j; i++) {
                if (lastReadId != -1 && lastReadId == adapter.getTimestamp(i)) {
                    pos = i;
                    break;
                }
            }
            if (pos != -1 && adapter.isActivity(pos) && (readFromBottom || lastVisiblePos != 0)) {
                if (layoutManager.getHeight() == 0) {
                    // RecyclerView has not currently laid out, ignore padding.
                    layoutManager.scrollToPositionWithOffset(pos, lastVisibleTop);
                } else {
                    layoutManager.scrollToPositionWithOffset(pos, lastVisibleTop - layoutManager.getPaddingTop());
                }
            }
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        onLoadingFinished();
    }

    @Override
    public void onLoaderReset(Loader<Data> loader) {
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        final ParcelableActivity activity = adapter.getActivity(position);
        final long[] accountIds = {activity.account_id};
        final long[] maxIds = {activity.min_position};
        getActivities(accountIds, maxIds, null);
    }

    @Override
    public void onMediaClick(IStatusViewHolder holder, View view, ParcelableMedia media, int position) {
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        final ParcelableStatus status = ParcelableActivity.getActivityStatus(adapter.getActivity(position));
        if (status == null) return;
        final Bundle options = Utils.createMediaViewerActivityOption(view);
        Utils.openMedia(getActivity(), status, media, options);
    }

    @Override
    public void onStatusActionClick(IStatusViewHolder holder, int id, int position) {
        final ParcelableStatus status = getActivityStatus(position);
        if (status == null) return;
        final FragmentActivity activity = getActivity();
        switch (id) {
            case R.id.reply_count: {
                final Intent intent = new Intent(INTENT_ACTION_REPLY);
                intent.setPackage(activity.getPackageName());
                intent.putExtra(EXTRA_STATUS, status);
                activity.startActivity(intent);
                break;
            }
            case R.id.retweet_count: {
                RetweetQuoteDialogFragment.show(getFragmentManager(), status);
                break;
            }
            case R.id.favorite_count: {
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                if (twitter == null) return;
                if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_id, status.id);
                } else {
                    twitter.createFavoriteAsync(status.account_id, status.id);
                }
                break;
            }
        }
    }

    @Override
    public void onActivityClick(ActivityTitleSummaryViewHolder holder, int position) {
        final ParcelableActivity activity = getAdapter().getActivity(position);
        if (activity == null) return;
        Utils.openUsers(getActivity(), Arrays.asList(ParcelableActivityUtils.getAfterFilteredSources(activity)));
    }

    @Override
    public void onStatusMenuClick(IStatusViewHolder holder, View menuView, int position) {

    }

    @Override
    public void onStatusClick(IStatusViewHolder holder, int position) {
        final ParcelableStatus status = getActivityStatus(position);
        Utils.openStatus(getContext(), status, null);
    }

    @Nullable
    private ParcelableStatus getActivityStatus(int position) {
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        return ParcelableActivity.getActivityStatus(adapter.getActivity(position));
    }

    @Override
    public void onStart() {
        super.onStart();
        final RecyclerView recyclerView = getRecyclerView();
        recyclerView.addOnScrollListener(mOnScrollListener);
        recyclerView.addOnScrollListener(mPauseOnScrollListener);
        mBus.register(mStatusesBusCallback);
    }

    @Override
    public void onStop() {
        mBus.unregister(mStatusesBusCallback);
        final RecyclerView recyclerView = getRecyclerView();
        recyclerView.removeOnScrollListener(mPauseOnScrollListener);
        recyclerView.removeOnScrollListener(mOnScrollListener);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        super.onDestroyView();
    }

    @Override
    public final boolean scrollToStart() {
        final boolean result = super.scrollToStart();
        if (result) {
            saveReadPosition(0);
            }
        return result;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        mNavigationHelper = new RecyclerViewNavigationHelper(recyclerView, layoutManager,
                adapter, this);

        adapter.setListener(this);
        mPauseOnScrollListener = new PauseRecyclerViewOnScrollListener(adapter.getMediaLoader().getImageLoader(), false, true);

        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
        showProgress();
    }

    protected Object createMessageBusCallback() {
        return new StatusesBusCallback();
    }

    protected abstract long[] getAccountIds();

    protected Data getAdapterData() {
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        return adapter.getData();
    }

    protected void setAdapterData(Data data) {
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        adapter.setData(data);
    }

    @ReadPositionTag
    @Nullable
    protected String getReadPositionTag() {
        return null;
    }

    protected abstract boolean hasMoreData(Data data);

    protected abstract Loader<Data> onCreateStatusesLoader(final Context context, final Bundle args,
                                                           final boolean fromUser);

    protected abstract void onLoadingFinished();

    protected void saveReadPosition(int position) {
        final String readPositionTag = getReadPositionTagWithAccounts();
        if (readPositionTag == null) return;
        if (position == RecyclerView.NO_POSITION) return;
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        final ParcelableActivity activity = adapter.getActivity(position);
        if (activity == null) return;
        if (mReadStateManager.setPosition(readPositionTag, activity.timestamp)) {
            mTwitterWrapper.setActivitiesAboutMeUnreadAsync(getAccountIds(), activity.timestamp);
        }
        mReadStateManager.setPosition(getCurrentReadPositionTag(), activity.timestamp, true);
    }

    @NonNull
    @Override
    protected Rect getExtraContentPadding() {
        final int paddingVertical = getResources().getDimensionPixelSize(R.dimen.element_spacing_small);
        return new Rect(0, paddingVertical, 0, paddingVertical);
    }

    @Override
    protected void setupRecyclerView(Context context, boolean compact) {
        if (compact) {
            super.setupRecyclerView(context, true);
            return;
        }
        final RecyclerView recyclerView = getRecyclerView();
        final AbsActivitiesAdapter<Data> adapter = getAdapter();
        // Dividers are drawn on bottom of view
        recyclerView.addItemDecoration(new DividerItemDecoration(context, getLayoutManager().getOrientation()) {

            @Override
            protected boolean isDividerEnabled(int childPos) {
                if (childPos == RecyclerView.NO_POSITION || childPos == adapter.getItemCount() - 1) {
                    return false;
                }
                final int itemViewType = adapter.getItemViewType(childPos);
                if (itemViewType == AbsActivitiesAdapter.ITEM_VIEW_TYPE_EMPTY) {
                    //
                    if (childPos != 0 && shouldUseDividerFor(adapter.getItemViewType(childPos + 1))) {
                        return true;
                    }
                    return false;
                }
                if (shouldUseDividerFor(itemViewType)) {
                    if (shouldUseDividerFor(adapter.getItemViewType(childPos + 1))) {
                        return true;
                    }
                }
                return false;
            }

            private boolean shouldUseDividerFor(int itemViewType) {
                return itemViewType != AbsActivitiesAdapter.ITEM_VIEW_TYPE_STATUS
                        && itemViewType != AbsActivitiesAdapter.ITEM_VIEW_TYPE_EMPTY;
            }
        });
    }

    private String getCurrentReadPositionTag() {
        final String tag = getReadPositionTagWithAccounts();
        if (tag == null) return null;
        return tag + "_current";
    }

    private String getReadPositionTagWithAccounts() {
        return Utils.getReadPositionTagWithAccounts(getReadPositionTag(), getAccountIds());
    }

    protected final class StatusesBusCallback {

        protected StatusesBusCallback() {
        }

        @Subscribe
        public void notifyStatusListChanged(StatusListChangedEvent event) {
            final AbsActivitiesAdapter<Data> adapter = getAdapter();
            adapter.notifyDataSetChanged();
        }

    }
}