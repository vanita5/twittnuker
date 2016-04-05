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

package de.vanita5.twittnuker.util.theme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.appthemeengine.tagprocessors.TagProcessor;

import de.vanita5.twittnuker.view.iface.IIconActionButton;

public class IconActionButtonTagProcessor extends TagProcessor {

    public static final String PREFIX_COLOR = "iab_color";
    public static final String PREFIX_COLOR_ACTIVATED = "iab_activated_color";
    public static final String PREFIX_COLOR_DISABLED = "iab_disabled_color";

    @NonNull
    private final String mPrefix;

    public IconActionButtonTagProcessor(@NonNull String prefix) {
        mPrefix = prefix;
    }

    @Override
    public boolean isTypeSupported(@NonNull View view) {
        return view instanceof IIconActionButton;
    }

    @Override
    public void process(@NonNull Context context, @Nullable String key, @NonNull View view, @NonNull String suffix) {
        final IIconActionButton iab = (IIconActionButton) view;
        final ColorResult colorResult = getColorFromSuffix(context, key, view, suffix);
        if (colorResult == null) return;
        switch (mPrefix) {
            case PREFIX_COLOR: {
                iab.setDefaultColor(colorResult.getColor());
                break;
            }
            case PREFIX_COLOR_ACTIVATED: {
                iab.setActivatedColor(colorResult.getColor());
                break;
            }
            case PREFIX_COLOR_DISABLED: {
                iab.setDisabledColor(colorResult.getColor());
                break;
            }
        }
    }
}