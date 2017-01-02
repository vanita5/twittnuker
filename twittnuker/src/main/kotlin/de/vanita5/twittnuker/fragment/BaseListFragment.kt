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

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v4.app.ListFragment
import android.support.v4.app.ListFragmentAccessor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
import android.widget.ProgressBar
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.view.ChameleonProgressBar
import de.vanita5.twittnuker.app.TwittnukerApplication
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_TAB_POSITION
import de.vanita5.twittnuker.fragment.iface.RefreshScrollTopInterface
import de.vanita5.twittnuker.util.AsyncTwitterWrapper
import de.vanita5.twittnuker.util.SharedPreferencesWrapper
import de.vanita5.twittnuker.util.Utils
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper

import javax.inject.Inject

open class BaseListFragment : ListFragment(), OnScrollListener, RefreshScrollTopInterface {

    @Inject
    lateinit var twitterWrapper: AsyncTwitterWrapper
    @Inject
    lateinit var preferences: SharedPreferencesWrapper
    var activityFirstCreated: Boolean = false
        private set
    var instanceStateSaved: Boolean = false
        private set
    var reachedBottom: Boolean = false
        private set
    private var notReachedBottomBefore = true

    override fun onAttach(context: Context) {
        super.onAttach(context)
        GeneralComponentHelper.build(context).inject(this)
    }

    val application: TwittnukerApplication
        get() = TwittnukerApplication.getInstance(activity)

    val contentResolver: ContentResolver?
        get() {
            val activity = activity
            if (activity != null) return activity.contentResolver
            return null
        }

    fun getSharedPreferences(name: String, mode: Int): SharedPreferences? {
        val activity = activity
        if (activity != null) return activity.getSharedPreferences(name, mode)
        return null
    }

    fun getSystemService(name: String): Any? {
        val activity = activity
        if (activity != null) return activity.getSystemService(name)
        return null
    }

    val tabPosition: Int
        get() {
            return arguments?.getInt(EXTRA_TAB_POSITION, -1) ?: -1
        }

    fun invalidateOptionsMenu() {
        val activity = activity ?: return
        activity.invalidateOptionsMenu()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        instanceStateSaved = savedInstanceState != null
        val lv = listView
        lv.setOnScrollListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityFirstCreated = true
    }

    override fun onDestroy() {
        super.onDestroy()
        activityFirstCreated = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        ((view.findViewById(ListFragmentAccessor.INTERNAL_PROGRESS_CONTAINER_ID) as ViewGroup).getChildAt(0) as ProgressBar).apply {
            val appearance = ChameleonProgressBar.Appearance()
            appearance.progressColor = Chameleon.getOverrideTheme(activity, activity).colorPrimary
            ChameleonProgressBar.Appearance.apply(this, appearance)
        }
        return view
    }

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
                          totalItemCount: Int) {
        val reached = firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount >= visibleItemCount

        if (reachedBottom != reached) {
            reachedBottom = reached
            if (reachedBottom && notReachedBottomBefore) {
                notReachedBottomBefore = false
                return
            }
            if (reachedBottom && listAdapter.count > visibleItemCount) {
                onReachedBottom()
            }
        }

    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {

    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        activityFirstCreated = false
        super.onStop()
    }

    override fun scrollToStart(): Boolean {
        Utils.scrollListToTop(listView)
        return true
    }

    override fun setSelection(position: Int) {
        Utils.scrollListToPosition(listView, position)
    }

    override fun triggerRefresh(): Boolean {
        return false
    }

    protected fun onReachedBottom() {

    }
}