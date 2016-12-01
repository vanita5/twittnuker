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
import android.text.Editable;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import de.vanita5.twittnuker.util.view.SimpleTextWatcher;

import static de.vanita5.twittnuker.constant.SharedPreferenceConstants.VALUE_THEME_NAME_DARK;

public class TextViewViewProcessor implements ViewProcessor<TextView, Void> {
    @Override
    public void process(@NonNull final Context context, @Nullable final String key,
                        @Nullable final TextView target, @Nullable Void extra) {
        if (target == null) return;
        switch (target.getId()) {
            case android.support.v7.appcompat.R.id.action_bar_title: {
                if (VALUE_THEME_NAME_DARK.equals(key)) return;
                target.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (Config.isLightToolbar(context, null, key, Config.toolbarColor(context, key, null))) {
                            target.setTextColor(Config.textColorPrimary(context, key));
                        } else {
                            target.setTextColor(Config.textColorPrimaryInverse(context, key));
                        }
                    }
                });
                break;
            }
            case android.support.v7.appcompat.R.id.action_bar_subtitle: {
                if (VALUE_THEME_NAME_DARK.equals(key)) return;
                target.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        if (Config.isLightToolbar(context, null, key, Config.toolbarColor(context, key, null))) {
                            target.setTextColor(Config.textColorSecondary(context, key));
                        } else {
                            target.setTextColor(Config.textColorSecondaryInverse(context, key));
                        }
                    }
                });
                break;
            }
        }
    }

}