/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.menu;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ActionMenuView;
import android.view.MenuItem;
import android.view.View;

import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable;
import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable.Style;

import java.lang.ref.WeakReference;

public class FavoriteItemProvider extends ActionProvider {
    private int mDefaultColor, mActivatedColor;
    private boolean mUseStar;
    private int mIcon;

    /**
     * Creates a new instance.
     *
     * @param context Context for accessing resources.
     */
    public FavoriteItemProvider(Context context) {
        super(context);
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    public void setUseStar(boolean useStar) {
        mUseStar = useStar;
    }

    public void setDefaultColor(int defaultColor) {
        mDefaultColor = defaultColor;
    }

    public void setActivatedColor(int activatedColor) {
        mActivatedColor = activatedColor;
    }

    public void invokeItem(MenuItem item, LikeAnimationDrawable.OnLikedListener listener) {
        if (MenuItemCompat.getActionProvider(item) != this) throw new IllegalArgumentException();
        final Drawable icon = item.getIcon();
        if (icon instanceof LikeAnimationDrawable) {
            ((LikeAnimationDrawable) icon).setOnLikedListener(listener);
            ((LikeAnimationDrawable) icon).start();
        }
    }

    public void setIcon(int icon) {
        mIcon = icon;
    }

    public void init(final ActionMenuView menuBar, MenuItem item) {
        if (MenuItemCompat.getActionProvider(item) != this) throw new IllegalArgumentException();
        final Drawable icon = ContextCompat.getDrawable(getContext(), mIcon);
        final LikeAnimationDrawable drawable = new LikeAnimationDrawable(icon, mDefaultColor,
                mActivatedColor, mUseStar ? Style.FAVORITE : Style.LIKE);
        drawable.mutate();
        drawable.setCallback(new ViewCallback(menuBar));
        item.setIcon(drawable);
    }

    public void setIsFavorite(MenuItem item, boolean isFavorite) {
        if (MenuItemCompat.getActionProvider(item) != this) throw new IllegalArgumentException();
        final Drawable icon = item.getIcon();
        if (icon instanceof LikeAnimationDrawable) {
            icon.mutate();
            icon.setColorFilter(isFavorite ? mActivatedColor : mDefaultColor, PorterDuff.Mode.SRC_ATOP);
        }
    }

    private static class ViewCallback implements Drawable.Callback {
        private final WeakReference<View> mViewRef;

        public ViewCallback(View view) {
            mViewRef = new WeakReference<>(view);
        }

        @Override
        public void invalidateDrawable(@NonNull Drawable who) {
            final View view = mViewRef.get();
            if (view == null) return;
            view.invalidate();
        }

        @Override
        public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
            final View view = mViewRef.get();
            if (view == null) return;
            view.postDelayed(what, when);
        }

        @Override
        public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
            final View view = mViewRef.get();
            if (view == null) return;
            view.post(what);
        }
    }
}