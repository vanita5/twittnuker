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

package de.vanita5.twittnuker.activity.iface

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ObjectAnimator
import android.util.Property
import android.view.animation.DecelerateInterpolator

interface IControlBarActivity {

    /**
     * 0: invisible, 1: visible
     */
    var controlBarOffset: Float
        get() = 0f
        set(value) {}

    val controlBarHeight: Int get() = 0

    fun setControlBarVisibleAnimate(visible: Boolean, listener: ControlBarShowHideHelper.ControlBarAnimationListener? = null) {}

    fun registerControlBarOffsetListener(listener: ControlBarOffsetListener) {}

    fun unregisterControlBarOffsetListener(listener: ControlBarOffsetListener) {}

    fun notifyControlBarOffsetChanged() {}

    interface ControlBarOffsetListener {
        fun onControlBarOffsetChanged(activity: IControlBarActivity, offset: Float)
    }

    class ControlBarShowHideHelper(private val activity: IControlBarActivity) {
        private var controlAnimationDirection: Int = 0
        private var currentControlAnimation: ObjectAnimator? = null

        private object ControlBarOffsetProperty : Property<IControlBarActivity, Float>(Float::class.java, null) {

            override fun set(obj: IControlBarActivity, value: Float) {
                obj.controlBarOffset = (value)
            }

            override fun get(obj: IControlBarActivity): Float {
                return obj.controlBarOffset
            }

        }

        interface ControlBarAnimationListener {
            fun onControlBarVisibleAnimationFinish(visible: Boolean)
        }

        fun setControlBarVisibleAnimate(visible: Boolean, listener: ControlBarAnimationListener? = null) {
            val newDirection = if (visible) 1 else -1
            if (controlAnimationDirection == newDirection) return
            if (currentControlAnimation != null && controlAnimationDirection != 0) {
                currentControlAnimation!!.cancel()
                currentControlAnimation = null
                controlAnimationDirection = newDirection
            }
            val animator: ObjectAnimator
            val offset = activity.controlBarOffset
            if (visible) {
                if (offset >= 1) return
                animator = ObjectAnimator.ofFloat(activity, ControlBarOffsetProperty, offset, 1f)
            } else {
                if (offset <= 0) return
                animator = ObjectAnimator.ofFloat(activity, ControlBarOffsetProperty, offset, 0f)
            }
            animator.interpolator = DecelerateInterpolator()
            animator.addListener(object : AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}

                override fun onAnimationEnd(animation: Animator) {
                    controlAnimationDirection = 0
                    currentControlAnimation = null
                    listener?.onControlBarVisibleAnimationFinish(visible)
                }

                override fun onAnimationCancel(animation: Animator) {
                    controlAnimationDirection = 0
                    currentControlAnimation = null
                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            animator.duration = DURATION
            animator.start()
            currentControlAnimation = animator
            controlAnimationDirection = newDirection
        }

        companion object {

            private const val DURATION = 200L
        }
    }
}