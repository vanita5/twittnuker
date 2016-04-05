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

package de.vanita5.twittnuker.view.iface;

import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;

/**
 * A PageIndicator is responsible to show an visual indicator on the total views
 * number and the current visible view.
 */
@SuppressWarnings("unused")
public interface PagerIndicator extends ViewPager.OnPageChangeListener {
	/**
	 * Notify the indicator that the fragment list has changed.
	 */
	void notifyDataSetChanged();

	/**
	 * <p>
	 * Set the current page of both the ViewPager and indicator.
	 * </p>
     * <p/>
	 * <p>
	 * This <strong>must</strong> be used if you need to set the page before the
	 * views are drawn on screen (e.g., default start page).
	 * </p>
	 * 
	 * @param item
	 */
	void setCurrentItem(int item);

	/**
	 * Set a page change listener which will receive forwarded events.
	 * 
	 * @param listener
	 */
	void setOnPageChangeListener(ViewPager.OnPageChangeListener listener);

	/**
	 * Bind the indicator to a ViewPager.
	 * 
	 * @param view
	 */
	void setViewPager(ViewPager view);

	/**
	 * Bind the indicator to a ViewPager.
	 * 
	 * @param view
	 * @param initialPosition
	 */
	void setViewPager(ViewPager view, int initialPosition);

    interface TabListener {

        void onPageReselected(int position);

        void onPageSelected(int position);

        boolean onTabLongClick(int position);
    }

    /**
     * A TitleProvider provides the title to display according to a view.
     */
    interface TabProvider {

        int getCount();

        /**
         * Returns the icon of the view at position
         *
         * @param position
         * @return
         */
        Drawable getPageIcon(int position);

        /**
         * Returns the title of the view at position
         *
         * @param position
         * @return
         */
        CharSequence getPageTitle(int position);

        float getPageWidth(int position);
    }
}