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

package de.vanita5.twittnuker.fragment.support;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.dagger.component.DaggerBaseSupportDialogFragmentComponent;

import javax.inject.Inject;

public class BaseSupportDialogFragment extends DialogFragment implements Constants {

    public BaseSupportDialogFragment() {

    }

    public TwittnukerApplication getApplication() {
        final Activity activity = getActivity();
        if (activity != null) return (TwittnukerApplication) activity.getApplication();
        return null;
    }

    public ContentResolver getContentResolver() {
        final Activity activity = getActivity();
        if (activity != null) return activity.getContentResolver();
        return null;
    }

    public SharedPreferences getSharedPreferences(final String name, final int mode) {
        final Activity activity = getActivity();
        if (activity != null) return activity.getSharedPreferences(name, mode);
        return null;
    }

    public Object getSystemService(final String name) {
        final Activity activity = getActivity();
        if (activity != null) return activity.getSystemService(name);
        return null;
    }

    @Inject
    protected AsyncTwitterWrapper mTwitterWrapper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerBaseSupportDialogFragmentComponent.builder().applicationModule(TwittnukerApplication.getModule(context)).build().inject(this);
    }

    public void registerReceiver(final BroadcastReceiver receiver, final IntentFilter filter) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.registerReceiver(receiver, filter);
    }

    public void setProgressBarIndeterminateVisibility(final boolean visible) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.setProgressBarIndeterminateVisibility(visible);
    }

    public void unregisterReceiver(final BroadcastReceiver receiver) {
        final Activity activity = getActivity();
        if (activity == null) return;
        activity.unregisterReceiver(receiver);
    }
}