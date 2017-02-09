/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.util

import android.support.v7.widget.RecyclerView
import android.view.View

import de.vanita5.twittnuker.util.ContentScrollHandler.ContentListSupport
import de.vanita5.twittnuker.util.ContentScrollHandler.ViewCallback

class RecyclerViewScrollHandler<A>(contentListSupport: ContentListSupport<A>, viewCallback: ViewCallback?) : RecyclerView.OnScrollListener() {

    internal val scrollHandler: ContentScrollHandler<A> = ContentScrollHandler(contentListSupport, viewCallback)
    private var oldState = RecyclerView.SCROLL_STATE_IDLE

    var touchSlop: Int
        get() = scrollHandler.touchSlop
        set(value) {
            scrollHandler.touchSlop = value
        }

    var reversed: Boolean
        get() = scrollHandler.reversed
        set(value) {
            scrollHandler.reversed = value
        }

    val touchListener: View.OnTouchListener
        get() = scrollHandler.touchListener

    override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
        scrollHandler.handleScrollStateChanged(newState, RecyclerView.SCROLL_STATE_IDLE)
    }

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        val scrollState = recyclerView!!.scrollState
        scrollHandler.handleScroll(dy, scrollState, oldState, RecyclerView.SCROLL_STATE_IDLE)
        oldState = scrollState
    }

    class RecyclerViewCallback(private val recyclerView: RecyclerView) : ViewCallback {

        override val computingLayout: Boolean
            get() = recyclerView.isComputingLayout

        override fun post(runnable: Runnable) {
            recyclerView.post(runnable)
        }
    }
}