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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;

import de.vanita5.twittnuker.fragment.ThemedListPreferenceDialogFragmentCompat;
import de.vanita5.twittnuker.preference.iface.IDialogPreference;

public class ThemedListPreference extends ListPreference implements IDialogPreference {
    public ThemedListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ThemedListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ThemedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedListPreference(Context context) {
        super(context);
    }

    @Override
    public void displayDialog(PreferenceFragmentCompat fragment) {
        ThemedListPreferenceDialogFragmentCompat df = ThemedListPreferenceDialogFragmentCompat.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getFragmentManager(), getKey());
    }
}