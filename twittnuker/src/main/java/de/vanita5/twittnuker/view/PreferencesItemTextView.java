/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import org.jetbrains.annotations.NotNull;
import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonUtils;


public class PreferencesItemTextView extends FixedTextView {

    private static final int[] ACTIVATED_STATE_SET = {android.R.attr.state_activated};
    private static final int[] EMPTY_STATE_SET = {0};

    public PreferencesItemTextView(@NotNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    @Nullable
    @Override
    public Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme) {
        Appearance appearance = new Appearance();
        final int activatedColor = ChameleonUtils.getColorDependent(theme.getColorControlActivated());
        final int defaultColor = theme.getTextColorPrimary();
        appearance.setTextColor(new ColorStateList(new int[][]{ACTIVATED_STATE_SET, EMPTY_STATE_SET}, new int[]{activatedColor, defaultColor}));
        return appearance;
    }
}