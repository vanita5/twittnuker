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

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import de.vanita5.twittnuker.util.ThemeUtils;

public class ImageViewViewProcessor implements ViewProcessor<ImageView, Void> {
    @SuppressLint("PrivateResource")
    @Override
    public void process(@NonNull Context context, @Nullable String key, @Nullable ImageView target, @Nullable Void extra) {
        if (target == null) return;
        switch (target.getId()) {
            case android.support.v7.appcompat.R.id.action_mode_close_button: {
                target.setImageResource(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
                target.setColorFilter(ThemeUtils.getColorDependent(Config.toolbarColor(context, key, null)));
                break;
            }
        }
    }
}