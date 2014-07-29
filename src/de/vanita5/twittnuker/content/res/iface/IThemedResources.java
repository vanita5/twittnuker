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
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public interface IThemedResources {

	public static final String RESOURCES_LOGTAG = "Twidere.Resources";

	public void addDrawableInterceptor(final DrawableInterceptor interceptor);

	public interface DrawableInterceptor {

		public Drawable getDrawable(final Resources res, final int resId);
	}

	public static final class Helper {

		private final ArrayList<DrawableInterceptor> mDrawableInterceptors = new ArrayList<DrawableInterceptor>();
		private final Resources mResources;

		public Helper(final Resources res, final Context context, final int overrideThemeRes) {
			mResources = res;
		}

		public void addDrawableInterceptor(final DrawableInterceptor interceptor) {
			mDrawableInterceptors.add(interceptor);
		}

		public Drawable getDrawable(final int resId) throws NotFoundException {
			for (final DrawableInterceptor interceptor : mDrawableInterceptors) {
				final Drawable d = interceptor.getDrawable(mResources, resId);
				if (d != null) return d;
			}
			return null;
		}
	}

}