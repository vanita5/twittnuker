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

import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

public class ListScrollDistanceCalculator implements OnScrollListener {

	private ScrollDistanceListener mScrollDistanceListener;

	private boolean mListScrollStarted;
	private int mFirstVisibleItem;
	private int mFirstVisibleHeight;
	private int mFirstVisibleTop, mFirstVisibleBottom;
	private int mTotalScrollDistance;

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (view.getCount() == 0) return;
		switch (scrollState) {
			case SCROLL_STATE_IDLE: {
				mListScrollStarted = false;
				break;
			}
			case SCROLL_STATE_TOUCH_SCROLL: {
				final View firstChild = view.getChildAt(0);
				mFirstVisibleItem = view.getFirstVisiblePosition();
				mFirstVisibleTop = firstChild.getTop();
				mFirstVisibleBottom = firstChild.getBottom();
				mFirstVisibleHeight = firstChild.getHeight();
				mListScrollStarted = true;
				mTotalScrollDistance = 0;
				break;
			}
		}
	}

	public int getTotalScrollDistance() {
		return mTotalScrollDistance;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (totalItemCount == 0 || !mListScrollStarted) return;
		final View firstChild = view.getChildAt(0);
		final int firstVisibleTop = firstChild.getTop(), firstVisibleBottom = firstChild.getBottom();
		final int firstVisibleHeight = firstChild.getHeight();
		final int delta;
		if (firstVisibleItem > mFirstVisibleItem) {
			mFirstVisibleTop += mFirstVisibleHeight;
			delta = firstVisibleTop - mFirstVisibleTop;
		} else if (firstVisibleItem < mFirstVisibleItem) {
			mFirstVisibleBottom -= mFirstVisibleHeight;
			delta = firstVisibleBottom - mFirstVisibleBottom;
		} else {
			delta = firstVisibleBottom - mFirstVisibleBottom;
		}
		mTotalScrollDistance += delta;
		if (mScrollDistanceListener != null) {
			mScrollDistanceListener.onScrollDistanceChanged(delta, mTotalScrollDistance);
		}
		mFirstVisibleTop = firstVisibleTop;
		mFirstVisibleBottom = firstVisibleBottom;
		mFirstVisibleHeight = firstVisibleHeight;
		mFirstVisibleItem = firstVisibleItem;
	}

	public void setScrollDistanceListener(ScrollDistanceListener listener) {
		mScrollDistanceListener = listener;
	}

    public interface ScrollDistanceListener {
		void onScrollDistanceChanged(int delta, int total);
	}
}