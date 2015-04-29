/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.commonsware.cwac.merge.MergeAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState;

import org.mariotaku.querybuilder.Columns;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.OrderBy;
import org.mariotaku.querybuilder.SQLQueryBuilder;
import org.mariotaku.querybuilder.Table;
import org.mariotaku.querybuilder.query.SQLSelectQuery;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.support.TrendsSuggectionsFragment.TrendsAdapter;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedTrends;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.ExtendedFrameLayout;
import de.vanita5.twittnuker.view.iface.IExtendedView.OnFitSystemWindowsListener;

import static de.vanita5.twittnuker.util.Utils.getTableNameByUri;
import static de.vanita5.twittnuker.util.Utils.openTweetSearch;

public class QuickMenuFragment extends BaseSupportFragment implements OnFitSystemWindowsListener, OnItemClickListener {

    private ExtendedFrameLayout mQuickMenuContainer;
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
            final SQLSelectQuery selectQuery = SQLQueryBuilder.select(new Columns(CachedTrends.TIMESTAMP))
                    .from(new Table(table))
                    .orderBy(new OrderBy(CachedTrends.TIMESTAMP, false))
                    .limit(1)
                    .build();
            final Expression where = Expression.equals(CachedTrends.TIMESTAMP, selectQuery);
            return new CursorLoader(getActivity(), uri, CachedTrends.COLUMNS, where.getSQL(), null, null);
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
        mQuickMenuContainer.setOnFitSystemWindowsListener(this);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (mPreferences.getBoolean(KEY_QUICK_MENU_EXPANDED, false)) {
		} else {
		}
        final Context context = getThemedContext();
		mAdapter = new MergeAdapter();
        mTrendsAdapter = new TrendsAdapter(context);

        mAdapter.addView(Utils.newSectionView(context, R.string.trends), false);
		mAdapter.addAdapter(mTrendsAdapter);
		mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
		getLoaderManager().initLoader(LOADER_ID_TRENDS, null, mTrendsCallback);
	}

	@Override
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        return LayoutInflater.from(getThemedContext());
    }

    @Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quick_menu, container, false);
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
        mQuickMenuContainer = (ExtendedFrameLayout) view.findViewById(R.id.quick_menu_fragment);
		mListView = (ListView) view.findViewById(android.R.id.list);
		mSlidingUpPanel = (SlidingUpPanelLayout) view.findViewById(R.id.activities_drawer);
        mActivitiesConfigButton = (ImageButton) view.findViewById(R.id.activities_config_button);
		final View activitiesContainer = view.findViewById(R.id.activities_container);
//        ViewUtils.setBackground(activitiesContainer, ThemeUtils.getWindowBackground(getThemedContext()));
	}

    @Override
    public void onFitSystemWindows(Rect insets) {
        mQuickMenuContainer.setPadding(insets.left, insets.top, insets.right, insets.bottom);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ListAdapter adapter = mAdapter.getAdapter(position);
        if (adapter instanceof TrendsAdapter) {
            openTweetSearch(getActivity(), getAccountId(), (String) mAdapter.getItem(position));
        }
    }

    private long getAccountId() {
        return -1;
    }

    @Override
    public Context getThemedContext() {
		if (mThemedContext != null) return mThemedContext;
		final Context context = getActivity();
        final int currentThemeResource = ThemeUtils.getNoActionBarThemeResource(context);
        final int themeResource = ThemeUtils.getDrawerThemeResource(currentThemeResource);
        return mThemedContext = new ContextThemeWrapper(context, themeResource);
	}

}