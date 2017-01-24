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
import android.graphics.Rect
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_toolbar_tab_pages.*
import kotlinx.android.synthetic.main.fragment_toolbar_tab_pages.view.*
import de.vanita5.twittnuker.R
import de.vanita5.twittnuker.activity.LinkHandlerActivity
import de.vanita5.twittnuker.activity.LinkHandlerActivity.HideUiOnScroll
import de.vanita5.twittnuker.activity.iface.IControlBarActivity
import de.vanita5.twittnuker.activity.iface.IControlBarActivity.ControlBarOffsetListener
import de.vanita5.twittnuker.adapter.SupportTabsAdapter
import de.vanita5.twittnuker.constant.KeyboardShortcutConstants.*
import de.vanita5.twittnuker.fragment.iface.IBaseFragment
import de.vanita5.twittnuker.fragment.iface.IToolBarSupportFragment
import de.vanita5.twittnuker.fragment.iface.RefreshScrollTopInterface
import de.vanita5.twittnuker.fragment.iface.SupportFragmentCallback
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler
import de.vanita5.twittnuker.util.KeyboardShortcutsHandler.KeyboardShortcutCallback
import de.vanita5.twittnuker.view.TabPagerIndicator

abstract class AbsToolbarTabPagesFragment : BaseFragment(), RefreshScrollTopInterface,
        SupportFragmentCallback, IBaseFragment.SystemWindowsInsetsCallback, ControlBarOffsetListener,
        HideUiOnScroll, OnPageChangeListener, IToolBarSupportFragment, KeyboardShortcutCallback {

    private var pagerAdapter: SupportTabsAdapter? = null
    override val toolbar: Toolbar
        get() = toolbarContainer.toolbar
    private var mControlBarHeight: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val activity = activity
        pagerAdapter = SupportTabsAdapter(activity, childFragmentManager, null)
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 2
        viewPager.addOnPageChangeListener(this)
        toolbarTabs.setViewPager(viewPager)
        toolbarTabs.setTabDisplayOption(TabPagerIndicator.DisplayOption.LABEL)


        addTabs(pagerAdapter!!)
        toolbarContainer.setOnSizeChangedListener { view, w, h, oldw, oldh ->
            val pageLimit = viewPager.offscreenPageLimit
            val currentItem = viewPager.currentItem
            val count = pagerAdapter!!.count
            for (i in 0 until count) {
                if (i > currentItem - pageLimit - 1 || i < currentItem + pageLimit) {
                    val obj = pagerAdapter!!.instantiateItem(viewPager, i)
                    if (obj is IBaseFragment<*>) {
                        obj.requestFitSystemWindows()
                    }
                }
            }
        }
    }

    protected abstract fun addTabs(adapter: SupportTabsAdapter)

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is IControlBarActivity) {
            context.registerControlBarOffsetListener(this)
        }
    }

    override fun onDetach() {
        val activity = activity
        if (activity is IControlBarActivity) {
            activity.unregisterControlBarOffsetListener(this)
        }
        super.onDetach()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_toolbar_tab_pages, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val host = host
        if (host is AppCompatActivity) {
            host.setSupportActionBar(toolbar)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val o = pagerAdapter!!.instantiateItem(viewPager, viewPager.currentItem)
        if (o is Fragment) {
            o.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun scrollToStart(): Boolean {
        val fragment = currentVisibleFragment as? RefreshScrollTopInterface ?: return false
        fragment.scrollToStart()
        return true
    }

    override fun triggerRefresh(): Boolean {
        return false
    }

    override val currentVisibleFragment: Fragment?
        get() {
            val currentItem = viewPager.currentItem
            if (currentItem < 0 || currentItem >= pagerAdapter!!.count) return null
            return pagerAdapter!!.instantiateItem(viewPager, currentItem) as Fragment
        }

    override fun triggerRefresh(position: Int): Boolean {
        return false
    }

    override fun fitSystemWindows(insets: Rect) {
    }

    override fun getSystemWindowsInsets(insets: Rect): Boolean {
        if (toolbarTabs == null) return false
        insets.set(0, toolbarContainer.height, 0, 0)
        return true
    }

    override fun onControlBarOffsetChanged(activity: IControlBarActivity, offset: Float) {
        mControlBarHeight = activity.controlBarHeight
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {

    }

    override fun onPageScrollStateChanged(state: Int) {
        val activity = activity
        if (activity is LinkHandlerActivity) {
            activity.setControlBarVisibleAnimate(true)
        }
    }

    override var controlBarOffset: Float
        get() {
            if (toolbarContainer == null) return 0f
            return 1 + toolbarContainer.translationY / controlBarHeight
        }
        set(offset) {
            if (toolbarContainer == null) return
            val translationY = (offset - 1) * controlBarHeight
            toolbarContainer.translationY = translationY
            windowOverlay!!.translationY = translationY
        }

    override val controlBarHeight: Int
        get() = toolbar.measuredHeight

    override fun setupWindow(activity: FragmentActivity): Boolean {
        return false
    }

    override fun handleKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int, event: KeyEvent, metaState: Int): Boolean {
        if (handleFragmentKeyboardShortcutSingle(handler, keyCode, event, metaState)) return true
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        if (action != null) {
            when (action) {
                ACTION_NAVIGATION_PREVIOUS_TAB -> {
                    val previous = viewPager.currentItem - 1
                    if (previous >= 0 && previous < pagerAdapter!!.count) {
                        viewPager.setCurrentItem(previous, true)
                    }
                    return true
                }
                ACTION_NAVIGATION_NEXT_TAB -> {
                    val next = viewPager.currentItem + 1
                    if (next >= 0 && next < pagerAdapter!!.count) {
                        viewPager.setCurrentItem(next, true)
                    }
                    return true
                }
            }
        }
        return handler.handleKey(activity, null, keyCode, event, metaState)
    }

    override fun isKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int,
                                           event: KeyEvent, metaState: Int): Boolean {
        if (isFragmentKeyboardShortcutHandled(handler, keyCode, event, metaState)) return true
        val action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState)
        return ACTION_NAVIGATION_PREVIOUS_TAB == action || ACTION_NAVIGATION_NEXT_TAB == action
    }

    override fun handleKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int,
                                              repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        return handleFragmentKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState)
    }

    private val keyboardShortcutRecipient: Fragment?
        get() = currentVisibleFragment

    private fun handleFragmentKeyboardShortcutRepeat(handler: KeyboardShortcutsHandler, keyCode: Int,
                                                     repeatCount: Int, event: KeyEvent, metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.handleKeyboardShortcutRepeat(handler, keyCode,
                    repeatCount, event, metaState)
        }
        return false
    }

    private fun handleFragmentKeyboardShortcutSingle(handler: KeyboardShortcutsHandler, keyCode: Int,
                                                     event: KeyEvent, metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.handleKeyboardShortcutSingle(handler, keyCode,
                    event, metaState)
        }
        return false
    }

    private fun isFragmentKeyboardShortcutHandled(handler: KeyboardShortcutsHandler, keyCode: Int,
                                                  event: KeyEvent, metaState: Int): Boolean {
        val fragment = keyboardShortcutRecipient
        if (fragment is KeyboardShortcutCallback) {
            return fragment.isKeyboardShortcutHandled(handler, keyCode,
                    event, metaState)
        }
        return false
    }
}