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

package de.vanita5.twittnuker.adapter.decorator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;

    private int mOrientation;
    private Rect mPadding;
    private int mDecorationStart = -1, mDecorationEnd = -1, mDecorationEndOffset;

    public DividerItemDecoration(Context context, int orientation) {
        mPadding = new Rect();
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);
    }


    public void setDecorationStart(int start) {
        mDecorationStart = start;
    }

    public void setDecorationEnd(int end) {
        mDecorationEnd = end;
        mDecorationEndOffset = -1;
    }

    public void setDecorationEndOffset(int endOffset) {
        mDecorationEndOffset = endOffset;
        mDecorationEnd = -1;
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, State state) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void setPadding(int left, int top, int right, int bottom) {
        mPadding.set(left, top, right, bottom);
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
        if (mDivider == null) return;
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final int childPos = parent.getChildAdapterPosition(child);
            if (!isDividerEnabled(childPos)) continue;
            final int start = getDecorationStart(), end = getDecorationEnd(parent);
            if (start >= 0 && childPos < start || end >= 0 && childPos > end) continue;
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin +
                    Math.round(ViewCompat.getTranslationY(child));
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left + mPadding.left, top + mPadding.top, right - mPadding.right,
                    bottom - mPadding.bottom);
            mDivider.draw(c);
        }
    }

    protected boolean isDividerEnabled(int childPos) {
        return true;
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        if (mDivider == null) return;
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final int childPos = parent.getChildAdapterPosition(child);
            final int start = getDecorationStart(), end = getDecorationEnd(parent);
            if (!isDividerEnabled(childPos)) continue;
            if (start >= 0 && childPos < start || end >= 0 && childPos > end) continue;
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin +
                    Math.round(ViewCompat.getTranslationX(child));
            final int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left + mPadding.left, top + mPadding.top, right - mPadding.right,
                    bottom - mPadding.bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        if (mDivider == null) return;
        final int childPos = parent.getChildAdapterPosition(view);
        if (!isDividerEnabled(childPos)) return;
        final int start = getDecorationStart(), end = getDecorationEnd(parent);
        if (start >= 0 && childPos < start || end >= 0 && childPos > end) {
            outRect.setEmpty();
            return;
        }
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }

    private int getDecorationEnd(RecyclerView parent) {
        if (mDecorationEnd != -1) return mDecorationEnd;
        if (mDecorationEndOffset != -1) {
            final Adapter adapter = parent.getAdapter();
            return adapter.getItemCount() - 1 - mDecorationEndOffset;
        }
        return -1;
    }

    private int getDecorationStart() {
        return mDecorationStart;
    }
}