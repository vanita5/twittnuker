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

package de.vanita5.twittnuker.util;

import android.support.annotation.UiThread;
import android.view.View;

public class TwidereViewUtils {
    private TwidereViewUtils() {
    }

    @UiThread
    public static boolean hitView(float x, float y, View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return TwidereMathUtils.inRange(x, location[0], location[0] + view.getWidth(), TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE) &&
                TwidereMathUtils.inRange(y, location[1], location[1] + view.getHeight(), TwidereMathUtils.RANGE_INCLUSIVE_INCLUSIVE);
    }
}