/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

public class PullToRefreshVerticalRecyclerView extends PullToRefreshBase<RecyclerView> {

	@IdRes
	public static final int REFRESHABLE_VIEW_ID = 0x7f200001;

	public PullToRefreshVerticalRecyclerView(Context context) {
		super(context);
	}

	public PullToRefreshVerticalRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshVerticalRecyclerView(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshVerticalRecyclerView(Context context, Mode mode, AnimationStyle animStyle) {
		super(context, mode, animStyle);
	}

	@Override
	public Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected RecyclerView createRefreshableView(Context context, AttributeSet attrs) {
		final RecyclerView recyclerView = new RecyclerView(context, attrs);
		recyclerView.setId(REFRESHABLE_VIEW_ID);
		return recyclerView;
	}

	@Override
	protected boolean isReadyForPullStart() {
		final RecyclerView recyclerView = getRefreshableView();
		if (recyclerView.getChildCount() <= 0)
			return true;
		int firstVisiblePosition = recyclerView.getChildPosition(recyclerView.getChildAt(0));
		if (firstVisiblePosition == 0)
			return recyclerView.getChildAt(0).getTop() == 0;
		else
			return false;

	}

	@Override
	protected boolean isReadyForPullEnd() {
		final RecyclerView recyclerView = getRefreshableView();
		int lastVisiblePosition = recyclerView.getChildPosition(recyclerView.getChildAt(recyclerView.getChildCount() - 1));
		if (lastVisiblePosition >= recyclerView.getAdapter().getItemCount() - 1) {
			return recyclerView.getChildAt(recyclerView.getChildCount() - 1).getBottom() <= recyclerView.getBottom();
		}
		return false;
	}
}