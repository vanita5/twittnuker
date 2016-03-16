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

import android.content.Context;
import android.support.v7.preference.PreferenceFragmentCompat;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements Constants {

    @Inject
    protected KeyboardShortcutsHandler mKeyboardShortcutHandler;
    @Inject
    protected UserColorNameManager mUserColorNameManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        GeneralComponentHelper.build(context).inject(this);
    }


}