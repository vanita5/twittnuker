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
import android.graphics.Color;
import android.support.annotation.NonNull;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import de.vanita5.twittnuker.view.TabPagerIndicator;

public class TabPagerIndicatorViewProcessor implements ViewProcessor<TabPagerIndicator, Object> {
    @Override
    public void process(@NonNull Context context, String key, TabPagerIndicator view, Object extra) {
        final int primaryColor = Config.primaryColor(context, key);
        final boolean isDark = !ATEUtil.isColorLight(primaryColor);
        final int primaryColorDependent = isDark ? Color.WHITE : Color.BLACK;
        view.setIconColor(primaryColorDependent);
        view.setLabelColor(primaryColorDependent);
        if (Config.coloredActionBar(context, key)) {
            view.setStripColor(primaryColorDependent);
        } else {
            view.setStripColor(Config.accentColor(context, key));
        }
        view.updateAppearance();
    }
}