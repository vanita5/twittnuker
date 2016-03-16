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

package de.vanita5.twittnuker.activity;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.List;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.ResolveInfoListAdapter;
import de.vanita5.twittnuker.loader.IntentActivitiesLoader;

public class ActivityPickerActivity extends BaseSupportDialogActivity implements LoaderCallbacks<List<ResolveInfo>>,
        OnItemClickListener {

    private ResolveInfoListAdapter mAdapter;

    private ListView mListView;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mListView = (ListView) findViewById(android.R.id.list);
    }

    @Override
    public Loader<List<ResolveInfo>> onCreateLoader(final int id, final Bundle args) {
        final Intent intent = getIntent();
        final Intent extraIntent = intent.getParcelableExtra(EXTRA_INTENT);

        final String[] blacklist = intent.getStringArrayExtra(EXTRA_BLACKLIST);
        return new IntentActivitiesLoader(this, extraIntent, blacklist, 0);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final Intent intent = getIntent(), data = new Intent();
        data.putExtra(EXTRA_DATA, mAdapter.getItem(position));
        data.putExtra(EXTRA_INTENT, intent.getParcelableExtra(EXTRA_INTENT));
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onLoaderReset(final Loader<List<ResolveInfo>> loader) {
        mAdapter.clear();
    }

    @Override
    public void onLoadFinished(final Loader<List<ResolveInfo>> loader, final List<ResolveInfo> data) {
        mAdapter.clear();
        if (data != null) {
            mAdapter.addAll(data);
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_picker);
        mAdapter = new ResolveInfoListAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        getSupportLoaderManager().initLoader(0, null, this);
    }

}