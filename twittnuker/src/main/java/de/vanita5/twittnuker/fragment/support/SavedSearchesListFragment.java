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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import de.vanita5.twittnuker.adapter.SavedSearchesAdapter;
import de.vanita5.twittnuker.api.twitter.model.ResponseList;
import de.vanita5.twittnuker.api.twitter.model.SavedSearch;
import de.vanita5.twittnuker.loader.support.SavedSearchesLoader;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.message.SavedSearchDestroyedEvent;

import java.util.Collections;
import java.util.Comparator;

import static de.vanita5.twittnuker.util.IntentUtils.openTweetSearch;

public class SavedSearchesListFragment extends AbsContentListViewFragment<SavedSearchesAdapter> implements
        LoaderCallbacks<ResponseList<SavedSearch>>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final Comparator<SavedSearch> POSITION_COMPARATOR = new Comparator<SavedSearch>() {

        @Override
        public int compare(final SavedSearch object1, final SavedSearch object2) {
            return object1.getPosition() - object2.getPosition();
        }

    };

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final ListView listView = getListView();
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        getLoaderManager().initLoader(0, null, this);
        showProgress();
    }

    @Override
    public void onStop() {
        mBus.unregister(this);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @NonNull
    @Override
    protected SavedSearchesAdapter onCreateAdapter(Context context, boolean compact) {
        return new SavedSearchesAdapter(getActivity());
    }

    @Override
    public Loader<ResponseList<SavedSearch>> onCreateLoader(final int id, final Bundle args) {
        return new SavedSearchesLoader(getActivity(), getAccountKey());
    }

    public UserKey getAccountKey() {
        return getArguments().getParcelable(EXTRA_ACCOUNT_KEY);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> view, final View child, final int position, final long id) {
        final SavedSearch item = getAdapter().findItem(id);
        if (item == null) return false;
        DestroySavedSearchDialogFragment.show(getFragmentManager(), getAccountKey(), item.getId(), item.getName());
        return true;
    }

    @Override
    public void onItemClick(final AdapterView<?> view, final View child, final int position, final long id) {
        final SavedSearch item = getAdapter().findItem(id);
        if (item == null) return;
        openTweetSearch(getActivity(), getAccountKey(), item.getQuery());
    }

    @Override
    public void onLoaderReset(final Loader<ResponseList<SavedSearch>> loader) {
        getAdapter().setData(null);
    }

    @Override
    public void onLoadFinished(final Loader<ResponseList<SavedSearch>> loader, final ResponseList<SavedSearch> data) {
        if (data != null) {
            Collections.sort(data, POSITION_COMPARATOR);
        }
        getAdapter().setData(data);
        showContent();
        setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        if (isRefreshing()) return;
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean isRefreshing() {
        return getLoaderManager().hasRunningLoaders();
    }

    @Subscribe
    public void onSavedSearchDestroyed(SavedSearchDestroyedEvent event) {
        SavedSearchesAdapter adapter = getAdapter();
        adapter.removeItem(event.getAccountKey().getId(), event.getSearchId());
    }
}