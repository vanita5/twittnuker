/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanit.as>
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

package de.vanita5.twittnuker.activity.support;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.squareup.otto.Bus;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.iface.IControlBarActivity;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.fragment.iface.IBaseFragment.SystemWindowsInsetsCallback;
import de.vanita5.twittnuker.util.ActivityTracker;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import de.vanita5.twittnuker.util.ReadStateManager;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.dagger.ApplicationModule;
import de.vanita5.twittnuker.util.dagger.DaggerGeneralComponent;
import de.vanita5.twittnuker.view.iface.IExtendedView.OnFitSystemWindowsListener;

import java.util.ArrayList;

import javax.inject.Inject;

@SuppressLint("Registered")
public class BaseAppCompatActivity extends ThemedAppCompatActivity implements Constants,
        OnFitSystemWindowsListener, SystemWindowsInsetsCallback, IControlBarActivity,
        KeyboardShortcutCallback {

    // Utility classes
    @Inject
    protected KeyboardShortcutsHandler mKeyboardShortcutsHandler;
    @Inject
    protected ActivityTracker mActivityTracker;
    @Inject
    protected AsyncTwitterWrapper mTwitterWrapper;
    @Inject
    protected ReadStateManager mReadStateManager;
    @Inject
    protected Bus mBus;

    // Registered listeners
    private ArrayList<ControlBarOffsetListener> mControlBarOffsetListeners = new ArrayList<>();

    // Data fields
    private boolean mInstanceStateSaved;
    private boolean mIsVisible;
    private Rect mSystemWindowsInsets;
    private int mKeyMetaState;

    @Override
    public boolean getSystemWindowsInsets(Rect insets) {
        if (mSystemWindowsInsets == null) return false;
        insets.set(mSystemWindowsInsets);
        return true;
    }

    @Override
    public int getThemeColor() {
        return ThemeUtils.getUserAccentColor(this);
    }

    @Override
    public int getActionBarColor() {
        return ThemeUtils.getActionBarColor(this);
    }

    @Override
    public int getThemeResourceId() {
        return ThemeUtils.getNoActionBarThemeResource(this);
    }

    public TwittnukerApplication getTwittnukerApplication() {
        return (TwittnukerApplication) getApplication();
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    @Override
    public void onFitSystemWindows(Rect insets) {
        if (mSystemWindowsInsets == null)
            mSystemWindowsInsets = new Rect(insets);
        else {
            mSystemWindowsInsets.set(insets);
        }
        notifyControlBarOffsetChanged();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        if (KeyEvent.isModifierKey(keyCode)) {
            final int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                mKeyMetaState |= KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                mKeyMetaState &= ~KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (handleKeyboardShortcutSingle(mKeyboardShortcutsHandler, keyCode, event, mKeyMetaState))
            return true;
        return isKeyboardShortcutHandled(mKeyboardShortcutsHandler, keyCode, event, mKeyMetaState) || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (handleKeyboardShortcutRepeat(mKeyboardShortcutsHandler, keyCode, event.getRepeatCount(), event, mKeyMetaState))
            return true;
        return isKeyboardShortcutHandled(mKeyboardShortcutsHandler, keyCode, event, mKeyMetaState) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void startActivity(final Intent intent) {
        super.startActivity(intent);
    }

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return false;
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        return false;
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, int keyCode, int repeatCount, @NonNull KeyEvent event, int metaState) {
        return false;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(this)).build().inject(this);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mIsVisible = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mInstanceStateSaved = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        mInstanceStateSaved = true;
        super.onSaveInstanceState(outState);
    }

    @Override
    public void startActivityForResult(final Intent intent, final int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onStop() {
        mIsVisible = false;
        super.onStop();
    }

    @Override
    public void setControlBarOffset(float offset) {

    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible) {

    }

    @Override
    public void setControlBarVisibleAnimate(boolean visible, ControlBarShowHideHelper.ControlBarAnimationListener listener) {

    }

    @Override
    public float getControlBarOffset() {
        return 0;
    }

    @Override
    public int getControlBarHeight() {
        return 0;
    }

    @Override
    public void notifyControlBarOffsetChanged() {
        final float offset = getControlBarOffset();
        for (final ControlBarOffsetListener l : mControlBarOffsetListeners) {
            l.onControlBarOffsetChanged(this, offset);
        }
    }

    @Override
    public void registerControlBarOffsetListener(ControlBarOffsetListener listener) {
        mControlBarOffsetListeners.add(listener);
    }

    @Override
    public void unregisterControlBarOffsetListener(ControlBarOffsetListener listener) {
        mControlBarOffsetListeners.remove(listener);
    }

    public int getKeyMetaState() {
        return mKeyMetaState;
    }

}