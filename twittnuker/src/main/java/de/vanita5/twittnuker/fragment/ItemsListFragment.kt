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
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.LoaderManager.LoaderCallbacks
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.widget.RecyclerView
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.fragment_content_recyclerview.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.adapter.VariousItemsAdapter
import de.vanita5.twittnuker.adapter.iface.IUsersAdapter
import de.vanita5.twittnuker.annotation.Referral
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ITEMS
import de.vanita5.twittnuker.constant.SharedPreferenceConstants.KEY_NEW_DOCUMENT_API
import de.vanita5.twittnuker.model.ParcelableMedia
import de.vanita5.twittnuker.util.IntentUtils
import de.vanita5.twittnuker.util.LinkCreator
import de.vanita5.twittnuker.util.MenuUtils
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.view.ExtendedRecyclerView
import de.vanita5.twittnuker.view.holder.StatusViewHolder
import de.vanita5.twittnuker.view.holder.UserViewHolder
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder

class ItemsListFragment : AbsContentListRecyclerViewFragment<VariousItemsAdapter>(), LoaderCallbacks<List<*>> {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        registerForContextMenu(recyclerView)
        loaderManager.initLoader(0, null, this)
        setRefreshEnabled(false)
        showContent()
    }

    override fun onCreateAdapter(context: Context): VariousItemsAdapter {
        val adapter = VariousItemsAdapter(context)
        val dummyItemAdapter = adapter.dummyAdapter
        dummyItemAdapter.statusClickListener = object : IStatusViewHolder.SimpleStatusClickListener() {
            override fun onStatusClick(holder: IStatusViewHolder, position: Int) {
                val status = dummyItemAdapter.getStatus(position) ?: return
                IntentUtils.openStatus(getContext(), status, null)
            }

            override fun onItemActionClick(holder: RecyclerView.ViewHolder, id: Int, position: Int) {
                val context = getContext() ?: return
                val status = dummyItemAdapter.getStatus(position) ?: return
                AbsStatusesFragment.handleStatusActionClick(context, fragmentManager,
                        twitterWrapper, holder as StatusViewHolder, status, id)
            }

            override fun onItemMenuClick(holder: RecyclerView.ViewHolder, menuView: View, position: Int) {
                if (activity == null) return
                val view = layoutManager!!.findViewByPosition(position) ?: return
                recyclerView.showContextMenuForChild(view)
            }

            override fun onMediaClick(holder: IStatusViewHolder, view: View, media: ParcelableMedia, statusPosition: Int) {
                val status = dummyItemAdapter.getStatus(statusPosition)
                if (status == null || media == null) return
                IntentUtils.openMedia(activity, status, media, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API))
            }

            override fun onUserProfileClick(holder: IStatusViewHolder, position: Int) {
                val activity = activity
                val status = dummyItemAdapter.getStatus(position) ?: return
                IntentUtils.openUserProfile(activity, status.account_key, status.user_key,
                        status.user_screen_name, null, preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        Referral.TIMELINE_STATUS)
            }
        }
        dummyItemAdapter.userClickListener = object : IUsersAdapter.SimpleUserClickListener() {
            override fun onUserClick(holder: UserViewHolder, position: Int) {
                val user = dummyItemAdapter.getUser(position) ?: return
                IntentUtils.openUserProfile(getContext(), user, null,
                        preferences.getBoolean(KEY_NEW_DOCUMENT_API),
                        Referral.TIMELINE_STATUS)
            }
        }
        return adapter
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<List<*>> {
        return ItemsLoader(context, arguments)
    }

    override fun onLoadFinished(loader: Loader<List<*>>, data: List<*>) {
        adapter!!.setData(data)
    }

    override fun onLoaderReset(loader: Loader<List<*>>) {
        adapter!!.setData(null)
    }

    override var refreshing: Boolean = false
        get() = false

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        if (!userVisibleHint || menuInfo == null) return
        val inflater = MenuInflater(context)
        val contextMenuInfo = menuInfo as ExtendedRecyclerView.ContextMenuInfo?
        val position = contextMenuInfo!!.position
        val adapter = adapter
        when (adapter!!.getItemViewType(position)) {
            VariousItemsAdapter.VIEW_TYPE_STATUS -> {
                val dummyAdapter = adapter.dummyAdapter
                val status = dummyAdapter.getStatus(contextMenuInfo.position) ?: return
                inflater.inflate(R.menu.action_status, menu)
                MenuUtils.setupForStatus(context, preferences, menu, status,
                        twitterWrapper)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        if (!userVisibleHint) return false
        val contextMenuInfo = item!!.menuInfo as ExtendedRecyclerView.ContextMenuInfo
        val position = contextMenuInfo.position
        val adapter = adapter
        when (adapter!!.getItemViewType(position)) {
            VariousItemsAdapter.VIEW_TYPE_STATUS -> {
                val dummyAdapter = adapter.dummyAdapter
                val status = dummyAdapter.getStatus(position) ?: return false
                if (item.itemId == R.id.share) {
                    val shareIntent = Utils.createStatusShareIntent(activity, status)
                    val chooser = Intent.createChooser(shareIntent, getString(R.string.share_status))
                    Utils.addCopyLinkIntent(context, chooser, LinkCreator.getStatusWebLink(status))
                    startActivity(chooser)
                    return true
                }
                return MenuUtils.handleStatusClick(activity, this, fragmentManager,
                        userColorNameManager, twitterWrapper, status, item)
            }
        }
        return false
    }

    class ItemsLoader(context: Context, private val mArguments: Bundle) : AsyncTaskLoader<List<*>>(context) {

        override fun loadInBackground(): List<*> {
            return mArguments.getParcelableArrayList<Parcelable>(EXTRA_ITEMS)
        }

        override fun onStartLoading() {
            forceLoad()
        }
    }
}