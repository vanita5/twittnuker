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

package de.vanita5.twittnuker.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonUtils;
import org.mariotaku.chameleon.ChameleonView;

import de.vanita5.twittnuker.util.support.WindowSupport;

public class HomeDrawerLayout extends DrawerLayout implements ChameleonView, ChameleonView.StatusBarThemeable {

    private ShouldDisableDecider mShouldDisableDecider;
    private int mStartLockMode, mEndLockMode;

    public HomeDrawerLayout(Context context) {
        super(context);
    }

    public HomeDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setShouldDisableDecider(ShouldDisableDecider shouldDisableDecider) {
        mShouldDisableDecider = shouldDisableDecider;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mStartLockMode = getDrawerLockMode(GravityCompat.START);
                mEndLockMode = getDrawerLockMode(GravityCompat.END);
                if (isDrawerOpen(GravityCompat.START) || isDrawerOpen(GravityCompat.END)) {
                    // Opened, disable close if requested
                    if (mShouldDisableDecider != null && mShouldDisableDecider.shouldDisableTouch(ev)) {
                        setDrawerLockMode(LOCK_MODE_LOCKED_OPEN, GravityCompat.START);
                        setDrawerLockMode(LOCK_MODE_LOCKED_OPEN, GravityCompat.END);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                setDrawerLockMode(mStartLockMode, GravityCompat.START);
                setDrawerLockMode(mEndLockMode, GravityCompat.END);
                break;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
        Appearance appearance = new Appearance();
        WindowSupport.setStatusBarColor(ChameleonUtils.getActivity(context).getWindow(), Color.TRANSPARENT);
        appearance.setStatusBarBackgroundColor(ChameleonUtils.darkenColor(theme.getColorToolbar()));
        return appearance;
    }

    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        Appearance a = (Appearance) appearance;
        setStatusBarBackgroundColor(a.getStatusBarBackgroundColor());
    }

    @Override
    public boolean isStatusBarColorHandled() {
        return true;
    }

    public static class Appearance implements ChameleonView.Appearance {
        int statusBarBackgroundColor;

        public int getStatusBarBackgroundColor() {
            return statusBarBackgroundColor;
        }

        public void setStatusBarBackgroundColor(int statusBarBackgroundColor) {
            this.statusBarBackgroundColor = statusBarBackgroundColor;
        }
    }

    public interface ShouldDisableDecider {
        boolean shouldDisableTouch(MotionEvent e);
    }
}