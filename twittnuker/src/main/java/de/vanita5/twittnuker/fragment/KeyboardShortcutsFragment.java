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

package de.vanita5.twittnuker.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.KeyboardShortcutPreferenceCompatActivity;
import de.vanita5.twittnuker.constant.KeyboardShortcutConstants;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutSpec;

public class KeyboardShortcutsFragment extends BasePreferenceFragment implements KeyboardShortcutConstants {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_keyboard_shortcuts);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_keyboard_shortcuts, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reset: {
                final DialogFragment f = new ResetKeyboardShortcutConfirmDialogFragment();
                f.show(getFragmentManager(), "reset_keyboard_shortcut_confirm");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private static class KeyboardShortcutPreferenceCompat extends Preference {

        private final KeyboardShortcutsHandler mKeyboardShortcutHandler;
        private final String mContextTag, mAction;
        private final OnSharedPreferenceChangeListener mPreferencesChangeListener;

        public KeyboardShortcutPreferenceCompat(final Context context, final KeyboardShortcutsHandler handler,
                                                @Nullable final String contextTag, @NonNull final String action) {
            super(context);
            mKeyboardShortcutHandler = handler;
            mContextTag = contextTag;
            mAction = action;
            setPersistent(false);
            setTitle(KeyboardShortcutsHandler.getActionLabel(context, action));
            mPreferencesChangeListener = new OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
                    updateSummary();
                }
            };
            updateSummary();
        }

        @Override
        protected void onClick() {
            final Context context = getContext();
            final Intent intent = new Intent(context, KeyboardShortcutPreferenceCompatActivity.class);
            intent.putExtra(KeyboardShortcutPreferenceCompatActivity.EXTRA_CONTEXT_TAG, mContextTag);
            intent.putExtra(KeyboardShortcutPreferenceCompatActivity.EXTRA_KEY_ACTION, mAction);
            context.startActivity(intent);
        }

        private void updateSummary() {
            final KeyboardShortcutSpec spec = mKeyboardShortcutHandler.findKey(mAction);
            setSummary(spec != null ? spec.toKeyString() : null);
        }

        @Override
        public void onAttached() {
            super.onAttached();
            mKeyboardShortcutHandler.registerOnSharedPreferenceChangeListener(mPreferencesChangeListener);
        }

        @Override
        protected void onPrepareForRemoval() {
            mKeyboardShortcutHandler.unregisterOnSharedPreferenceChangeListener(mPreferencesChangeListener);
            super.onPrepareForRemoval();
        }


    }

    public static class ResetKeyboardShortcutConfirmDialogFragment extends BaseDialogFragment
            implements OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    keyboardShortcutsHandler.reset();
                    break;
                }
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.reset_keyboard_shortcuts_confirm);
            builder.setPositiveButton(android.R.string.ok, this);
            builder.setNegativeButton(android.R.string.cancel, this);
            return builder.create();
        }
    }
}