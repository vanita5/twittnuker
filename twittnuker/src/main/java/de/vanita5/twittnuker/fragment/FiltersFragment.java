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

package de.vanita5.twittnuker.fragment;

import android.os.Bundle;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.SupportTabsAdapter;
import de.vanita5.twittnuker.fragment.AbsToolbarTabPagesFragment;
import de.vanita5.twittnuker.fragment.BaseFiltersFragment.FilteredKeywordsFragment;
import de.vanita5.twittnuker.fragment.BaseFiltersFragment.FilteredLinksFragment;
import de.vanita5.twittnuker.fragment.BaseFiltersFragment.FilteredSourcesFragment;
import de.vanita5.twittnuker.fragment.BaseFiltersFragment.FilteredUsersFragment;

public class FiltersFragment extends AbsToolbarTabPagesFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected void addTabs(SupportTabsAdapter adapter) {
        adapter.addTab(FilteredUsersFragment.class, null, getString(R.string.users), null, 0, null);
        adapter.addTab(FilteredKeywordsFragment.class, null, getString(R.string.keywords), null, 1, null);
        adapter.addTab(FilteredSourcesFragment.class, null, getString(R.string.sources), null, 2, null);
        adapter.addTab(FilteredLinksFragment.class, null, getString(R.string.links), null, 3, null);
    }

}