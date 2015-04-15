/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;

import de.vanita5.twittnuker.adapter.iface.IContentCardAdapter;

public class ContentListScrollListener extends OnScrollListener {

	private int mScrollState;
	private int mScrollSum;
	private int mTouchSlop;

    private ContentListSupport mContentListSupport;
    private OnScrollListener mOnScrollListener;

    public ContentListScrollListener(@NonNull ContentListSupport contentListSupport) {
        mContentListSupport = contentListSupport;
    }

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(recyclerView, newState);
        }
        if (mScrollState != RecyclerView.SCROLL_STATE_IDLE) {
            notifyScrollStateChanged(recyclerView);
        }
		mScrollState = newState;
	}

	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrolled(recyclerView, dx, dy);
        }
		//Reset mScrollSum when scrolling in reverse direction
		if (dy * mScrollSum < 0) {
			mScrollSum = 0;
		}
		mScrollSum += dy;
		if (Math.abs(mScrollSum) > mTouchSlop) {
            mContentListSupport.setControlVisible(dy < 0);
			mScrollSum = 0;
		}
        if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            notifyScrollStateChanged(recyclerView);
		}
    }

    public void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

	public void setTouchSlop(int touchSlop) {
		mTouchSlop = touchSlop;
	}

    private void notifyScrollStateChanged(RecyclerView recyclerView) {
        final IContentCardAdapter adapter = mContentListSupport.getAdapter();
        final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (!mContentListSupport.isRefreshing() && adapter.isLoadMoreSupported() && !adapter.isLoadMoreIndicatorVisible()
                && layoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1) {
            mContentListSupport.onLoadMoreContents();
        }
    }

    public static interface ContentListSupport {

        IContentCardAdapter getAdapter();

		boolean isRefreshing();

        void onLoadMoreContents();

		void setControlVisible(boolean visible);

	}
}