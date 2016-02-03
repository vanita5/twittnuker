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

package android.support.v7.widget;

import android.view.View;

public class LinearLayoutManagerAccessor {

    public static OrientationHelper getOrientationHelper(LinearLayoutManager llm) {
        return llm.mOrientationHelper;
    }

    public static void ensureLayoutState(LinearLayoutManager llm) {
        llm.ensureLayoutState();
    }

    public static boolean getShouldReverseLayout(LinearLayoutManager llm) {
        return llm.mShouldReverseLayout;
    }

    public static View findOneVisibleChild(LinearLayoutManager llm, int fromIndex, int toIndex, boolean completelyVisible, boolean acceptPartiallyVisible) {
        return llm.findOneVisibleChild(fromIndex, toIndex, completelyVisible, acceptPartiallyVisible);
    }
}