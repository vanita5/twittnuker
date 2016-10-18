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

package de.vanita5.twittnuker.fragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.Loader
import android.view.View
import android.widget.AdapterView
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_content_listview.*
import de.vanita5.twittnuker.library.twitter.model.ResponseList
import de.vanita5.twittnuker.library.twitter.model.SavedSearch
import de.vanita5.twittnuker.adapter.SavedSearchesAdapter
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import de.vanita5.twittnuker.loader.SavedSearchesLoader
import de.vanita5.twittnuker.model.UserKey
import de.vanita5.twittnuker.model.message.SavedSearchDestroyedEvent
import de.vanita5.twittnuker.util.IntentUtils.openTweetSearch
import java.util.*

class SavedSearchesListFragment : AbsContentListViewFragment<SavedSearchesAdapter>(),
        LoaderCallbacks<ResponseList<SavedSearch>?>, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView.onItemClickListener = this
        listView.onItemLongClickListener = this
        loaderManager.initLoader(0, null, this)
        showProgress()
    }

    override fun onStop() {
        bus.unregister(this)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        bus.register(this)
    }

    override fun onCreateAdapter(context: Context): SavedSearchesAdapter {
        return SavedSearchesAdapter(activity)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ResponseList<SavedSearch>?> {
        return SavedSearchesLoader(activity, accountKey)
    }

    val accountKey: UserKey
        get() = arguments.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)

    override fun onItemLongClick(view: AdapterView<*>, child: View, position: Int, id: Long): Boolean {
        val item = adapter!!.findItem(id) ?: return false
        DestroySavedSearchDialogFragment.show(fragmentManager, accountKey, item.id, item.name)
        return true
    }

    override fun onItemClick(view: AdapterView<*>, child: View, position: Int, id: Long) {
        val item = adapter!!.findItem(id) ?: return
        openTweetSearch(activity, accountKey, item.query)
    }

    override fun onLoaderReset(loader: Loader<ResponseList<SavedSearch>?>) {
        adapter!!.setData(null)
    }

    override fun onLoadFinished(loader: Loader<ResponseList<SavedSearch>?>, data: ResponseList<SavedSearch>?) {
        if (data != null) {
            Collections.sort(data, POSITION_COMPARATOR)
        }
        adapter!!.setData(data)
        showContent()
        refreshing = false
    }

    override fun onRefresh() {
        if (refreshing) return
        loaderManager.restartLoader(0, null, this)
    }

    override var refreshing: Boolean
        get() = loaderManager.hasRunningLoaders()
        set(value) {
            super.refreshing = value
        }

    @Subscribe
    fun onSavedSearchDestroyed(event: SavedSearchDestroyedEvent) {
        val adapter = adapter
        adapter!!.removeItem(event.accountKey, event.searchId)
    }

    companion object {

        private val POSITION_COMPARATOR = Comparator<de.vanita5.twittnuker.library.twitter.model.SavedSearch> { object1, object2 -> object1.position - object2.position }
    }
}