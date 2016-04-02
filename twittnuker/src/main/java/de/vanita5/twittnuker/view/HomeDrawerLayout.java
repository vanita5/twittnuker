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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.inflation.ViewInterface;

/**
 * @author Aidan Follestad (afollestad)
 */
public class HomeDrawerLayout extends DrawerLayout implements ViewInterface {

    private ShouldDisableDecider mShouldDisableDecider;
    private int state;
    private int mStartLockMode, mEndLockMode;

    public HomeDrawerLayout(Context context) {
        super(context);
        init(context, null);
    }

    public HomeDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public HomeDrawerLayout(Context context, AttributeSet attrs, @Nullable ATEActivity keyContext) {
        super(context, attrs);
        init(context, keyContext);
    }

    private void init(Context context, @Nullable ATEActivity keyContext) {
        if (keyContext == null && context instanceof ATEActivity) {
            keyContext = (ATEActivity) context;
        }
        final String key = keyContext != null ? keyContext.getATEKey() : null;
        if (Config.coloredStatusBar(context, key)) {
            // Sets the status bar overlayed by the DrawerLayout
            setStatusBarBackgroundColor(Config.statusBarColor(context, key));
            if (context instanceof Activity && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final Activity activity = (Activity) context;
                // Sets Activity status bar to transparent, DrawerLayout overlays a color.
                activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
                ATE.invalidateLightStatusBar(activity, key);
            }
        }
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
    public boolean setsStatusBarColor() {
        return true;
    }

    @Override
    public boolean setsToolbarColor() {
        return false;
    }


    public interface ShouldDisableDecider {
        boolean shouldDisableTouch(MotionEvent e);
    }
}