/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.content.res.iface;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.util.ArrayList;

import de.vanita5.twittnuker.util.theme.ActionIconsInterceptor;
import de.vanita5.twittnuker.util.theme.ActivityIconsInterceptor;
import de.vanita5.twittnuker.util.theme.WhiteDrawableInterceptor;

public interface IThemedResources {

	public static final String RESOURCES_LOGTAG = "Twidere.Resources";

	public void addDrawableInterceptor(final DrawableInterceptor interceptor);

	public static interface DrawableInterceptor {
		public Drawable getDrawable(int id);

		public boolean shouldIntercept(int id);
	}

	public static final class Helper {

		private final ArrayList<DrawableInterceptor> mDrawableInterceptors = new ArrayList<DrawableInterceptor>();

		public Helper(final Resources res, final Context context) {
            final DisplayMetrics dm = res.getDisplayMetrics();
            addDrawableInterceptor(new ActionIconsInterceptor(context, dm));
            addDrawableInterceptor(new ActivityIconsInterceptor(context, dm));
            addDrawableInterceptor(new WhiteDrawableInterceptor(res));
		}

		public void addDrawableInterceptor(final DrawableInterceptor interceptor) {
			mDrawableInterceptors.add(interceptor);
		}

		public Drawable getDrawable(final int id) throws Resources.NotFoundException {
			for (final DrawableInterceptor interceptor : mDrawableInterceptors) {
				if (interceptor.shouldIntercept(id)) return interceptor.getDrawable(id);
			}
			return null;
		}
	}

}