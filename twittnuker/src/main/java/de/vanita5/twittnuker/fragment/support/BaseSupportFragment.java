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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManagerAccessor;
import android.support.v4.text.BidiFormatter;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.LayoutInflaterFactory;
import android.view.LayoutInflater;
import android.view.View;

import com.squareup.otto.Bus;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.iface.IThemedActivity;
import de.vanita5.twittnuker.activity.support.BaseAppCompatActivity;
import de.vanita5.twittnuker.fragment.iface.IBaseFragment;
import de.vanita5.twittnuker.util.AsyncTaskManager;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.DebugModeUtils;
import de.vanita5.twittnuker.util.ErrorInfoStore;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.MultiSelectManager;
import de.vanita5.twittnuker.util.NotificationManagerWrapper;
import de.vanita5.twittnuker.util.ReadStateManager;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.ThemedLayoutInflaterFactory;
import de.vanita5.twittnuker.util.TwidereValidator;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

public class BaseSupportFragment extends Fragment implements IBaseFragment, Constants {

    // Utility classes
    @Inject
    protected AsyncTwitterWrapper mTwitterWrapper;
    @Inject
    protected ReadStateManager mReadStateManager;
    @Inject
    protected MediaLoaderWrapper mMediaLoader;
    @Inject
    protected Bus mBus;
    @Inject
    protected AsyncTaskManager mAsyncTaskManager;
    @Inject
    protected MultiSelectManager mMultiSelectManager;
    @Inject
    protected UserColorNameManager mUserColorNameManager;
    @Inject
    protected SharedPreferencesWrapper mPreferences;
    @Inject
    protected NotificationManagerWrapper mNotificationManager;
    @Inject
    protected BidiFormatter mBidiFormatter;
    @Inject
    protected ErrorInfoStore mErrorInfoStore;
    @Inject
    TwidereValidator mValidator;

    public BaseSupportFragment() {

    }

    @Override
    public final void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onBaseViewCreated(view, savedInstanceState);
        requestFitSystemWindows();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        GeneralComponentHelper.build(context).inject(this);
    }

    public ContentResolver getContentResolver() {
        final Activity activity = getActivity();
        if (activity != null) return activity.getContentResolver();
        return null;
    }

    public void invalidateOptionsMenu() {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        activity.supportInvalidateOptionsMenu();
    }

    public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.registerReceiver(receiver, filter);
    }

    public void setProgressBarIndeterminateVisibility(final boolean visible) {
        final Activity activity = getActivity();
        if (activity instanceof BaseAppCompatActivity) {
            activity.setProgressBarIndeterminateVisibility(visible);
        }
    }

    public void unregisterReceiver(final BroadcastReceiver receiver) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.unregisterReceiver(receiver);
    }

    @Override
    public Bundle getExtraConfiguration() {
        return null;
    }

    @Override
    public int getTabPosition() {
        final Bundle args = getArguments();
        return args != null ? args.getInt(EXTRA_TAB_POSITION, -1) : -1;
    }

    @Override
    public void requestFitSystemWindows() {
        final Activity activity = getActivity();
        final Fragment parentFragment = getParentFragment();
        final SystemWindowsInsetsCallback callback;
        if (parentFragment instanceof SystemWindowsInsetsCallback) {
            callback = (SystemWindowsInsetsCallback) parentFragment;
        } else if (activity instanceof SystemWindowsInsetsCallback) {
            callback = (SystemWindowsInsetsCallback) activity;
        } else {
            return;
        }
        final Rect insets = new Rect();
        if (callback.getSystemWindowsInsets(insets)) {
            fitSystemWindows(insets);
        }
    }

    @Override
    public void onBaseViewCreated(View view, Bundle savedInstanceState) {

    }

    @Override
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        if (!(activity instanceof IThemedActivity)) {
            return super.getLayoutInflater(savedInstanceState);
        }
        final LayoutInflater inflater = activity.getLayoutInflater().cloneInContext(getThemedContext());
        getChildFragmentManager(); // Init if needed; use raw implementation below.
        final LayoutInflaterFactory delegate = FragmentManagerAccessor.getLayoutInflaterFactory(getChildFragmentManager());
        LayoutInflaterCompat.setFactory(inflater, new ThemedLayoutInflaterFactory((IThemedActivity) activity, delegate));
        return inflater;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugModeUtils.watchReferenceLeak(this);
    }

    public Context getThemedContext() {
        return getActivity();
    }

    protected void fitSystemWindows(Rect insets) {
        final View view = getView();
        if (view != null) {
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom);
        }
    }
}