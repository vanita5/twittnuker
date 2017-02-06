/*
 *  Twittnuker - Twitter client for Android
 *
 *  Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 *  This program incorporates a modified version of Twidere.
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.vanita5.twittnuker.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout


open class ContainerView(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    private var attachedToWindow: Boolean = false

    var viewController: ViewController? = null
        set(vc) {
            field?.let { vc ->
                if (vc.attached) {
                    vc.attached = false
                    vc.onDetached()
                }
                val view = vc.view
                vc.onDestroyView(view)
                this.removeView(view)
                vc.onDestroy()
            }
            field = vc
            if (vc != null) {
                if (vc.attached) {
                    throw IllegalStateException("ViewController has already attached")
                }
                vc.context = context
                val view = vc.onCreateView(this)
                this.addView(view)
                vc.view = view
                vc.onCreate()
                vc.onViewCreated(view)
                if (attachedToWindow) {
                    vc.attached = true
                    vc.onAttached()
                }
            }
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachedToWindow = true
        viewController?.let { vc ->
            if (!vc.windowAttached) {
                vc.windowAttached = true
                vc.onWindowAttached()
            }
        }
    }

    override fun onDetachedFromWindow() {
        attachedToWindow = false
        viewController?.let { vc ->
            if (vc.attached) {
                vc.windowAttached = false
                vc.onWindowDetached()
            }
        }
        super.onDetachedFromWindow()
    }


    abstract class ViewController {

        lateinit var context: Context
            internal set
        lateinit var view: View
            internal set
        var attached: Boolean = false
            internal set
        var windowAttached: Boolean = false
            internal set

        open fun onCreate() {}
        open fun onDestroy() {}

        open fun onAttached() {}
        open fun onDetached() {}

        open fun onWindowAttached() {}
        open fun onWindowDetached() {}

        abstract fun onCreateView(parent: ContainerView): View

        open fun onDestroyView(view: View) {}

        open fun onViewCreated(view: View) {}
    }

}