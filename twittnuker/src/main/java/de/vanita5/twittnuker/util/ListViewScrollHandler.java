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

package de.vanita5.twittnuker.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.AbsListView;

import de.vanita5.twittnuker.util.support.ViewSupport;

public class ListViewScrollHandler extends ContentScrollHandler implements AbsListView.OnScrollListener,
        ListScrollDistanceCalculator.ScrollDistanceListener {
    private final ListScrollDistanceCalculator mCalculator;
    @Nullable
    private AbsListView.OnScrollListener mOnScrollListener;
    private int mDy;
    private int mOldState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;

    public ListViewScrollHandler(@NonNull ContentListSupport contentListSupport, @Nullable ViewCallback viewCallback) {
        super(contentListSupport, viewCallback);
        mCalculator = new ListScrollDistanceCalculator();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mCalculator.onScrollStateChanged(view, scrollState);
        mCalculator.setScrollDistanceListener(this);
        handleScrollStateChanged(scrollState, SCROLL_STATE_IDLE);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mCalculator.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        final int scrollState = getScrollState();
        handleScroll(mDy, scrollState, mOldState, SCROLL_STATE_IDLE);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    @Nullable
    public AbsListView.OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    public void setOnScrollListener(@Nullable AbsListView.OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    public int getTotalScrollDistance() {
        return mCalculator.getTotalScrollDistance();
    }

    @Override
    public void onScrollDistanceChanged(int delta, int total) {
        mDy = -delta;
        final int scrollState = getScrollState();
        handleScroll(mDy, scrollState, mOldState, SCROLL_STATE_IDLE);
        mOldState = scrollState;
    }

    public static class ListViewCallback implements ViewCallback {
        private final AbsListView listView;

        public ListViewCallback(AbsListView listView) {
            this.listView = listView;
        }

        @Override
        public boolean getComputingLayout() {
            return ViewSupport.isInLayout(listView);
        }

        @Override
        public void post(@NonNull Runnable runnable) {
            listView.post(runnable);
        }
    }
}