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

package de.vanita5.twittnuker.fragment.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.support.TrendsSuggectionsFragment.TrendsAdapter;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.accessor.ViewAccessor;

import static de.vanita5.twittnuker.util.Utils.getTableNameByUri;

public class QuickMenuFragment extends BaseSupportFragment {

	private SharedPreferences mPreferences;
	private Context mThemedContext;
	private ListView mListView;
	private SlidingUpPanelLayout mSlidingUpPanel;
    private ImageButton mActivitiesConfigButton;

	private MergeAdapter mAdapter;
	private TrendsAdapter mTrendsAdapter;

	private static final int LOADER_ID_TRENDS = 1;

	private final LoaderCallbacks<Cursor> mTrendsCallback = new LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
			final Uri uri = CachedTrends.Local.CONTENT_URI;
			final String table = getTableNameByUri(uri);
			final String where = table != null ? CachedTrends.TIMESTAMP + " = " + "(SELECT " + CachedTrends.TIMESTAMP
					+ " FROM " + table + " ORDER BY " + CachedTrends.TIMESTAMP + " DESC LIMIT 1)" : null;
			return new CursorLoader(getActivity(), uri, CachedTrends.COLUMNS, where, null, null);
		}

		@Override
		public void onLoaderReset(final Loader<Cursor> loader) {
			mTrendsAdapter.swapCursor(null);
		}

		@Override
		public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
			mTrendsAdapter.swapCursor(data);
		}

	};

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (mPreferences.getBoolean(KEY_QUICK_MENU_EXPANDED, false)) {
		} else {
		}
		mAdapter = new MergeAdapter();
		mTrendsAdapter = new TrendsAdapter(getThemedContext());
		mAdapter.addAdapter(mTrendsAdapter);
		mListView.setAdapter(mAdapter);
		getLoaderManager().initLoader(LOADER_ID_TRENDS, null, mTrendsCallback);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return LayoutInflater.from(getThemedContext()).inflate(R.layout.fragment_quick_menu, container, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(KEY_QUICK_MENU_EXPANDED, mSlidingUpPanel.getPanelState() == PanelState.EXPANDED);
		editor.apply();
	}

	@Override
    public void onBaseViewCreated(final View view, final Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
		mListView = (ListView) view.findViewById(android.R.id.list);
		mSlidingUpPanel = (SlidingUpPanelLayout) view.findViewById(R.id.activities_drawer);
        mActivitiesConfigButton = (ImageButton) view.findViewById(R.id.activities_config_button);
		final View activitiesContainer = view.findViewById(R.id.activities_container);
		ViewAccessor.setBackground(activitiesContainer, ThemeUtils.getWindowBackground(getThemedContext()));
	}

	private Context getThemedContext() {
		if (mThemedContext != null) return mThemedContext;
		final Context context = getActivity();
        final int currentThemeResource = ThemeUtils.getThemeResource(context);
        final int themeResource = ThemeUtils.getDrawerThemeResource(currentThemeResource);
        return mThemedContext = new ContextThemeWrapper(context, themeResource);
	}

}