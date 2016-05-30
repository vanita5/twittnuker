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

package de.vanita5.twittnuker.activity.iface;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.util.Property;
import android.view.animation.DecelerateInterpolator;

public interface IControlBarActivity {

    /**
     * @param offset 0: invisible, 1: visible
     */
    void setControlBarOffset(float offset);

    void setControlBarVisibleAnimate(boolean visible);

    void setControlBarVisibleAnimate(boolean visible, ControlBarShowHideHelper.ControlBarAnimationListener listener);

    float getControlBarOffset();

    int getControlBarHeight();

    void notifyControlBarOffsetChanged();

    void registerControlBarOffsetListener(ControlBarOffsetListener listener);

    void unregisterControlBarOffsetListener(ControlBarOffsetListener listener);

    interface ControlBarOffsetListener {
        void onControlBarOffsetChanged(IControlBarActivity activity, float offset);
    }

    final class ControlBarShowHideHelper {

        private static final long DURATION = 200L;

        private final IControlBarActivity mActivity;
        private int mControlAnimationDirection;
        private ObjectAnimator mCurrentControlAnimation;

        public ControlBarShowHideHelper(IControlBarActivity activity) {
            mActivity = activity;
        }

        private static class ControlBarOffsetProperty extends Property<IControlBarActivity, Float> {
            public static final ControlBarOffsetProperty SINGLETON = new ControlBarOffsetProperty();

            @Override
            public void set(IControlBarActivity object, Float value) {
                object.setControlBarOffset(value);
            }

            public ControlBarOffsetProperty() {
                super(Float.TYPE, null);
            }

            @Override
            public Float get(IControlBarActivity object) {
                return object.getControlBarOffset();
            }
        }

        public interface ControlBarAnimationListener {
            void onControlBarVisibleAnimationFinish(boolean visible);
        }

        public void setControlBarVisibleAnimate(boolean visible) {
            setControlBarVisibleAnimate(visible, null);
        }

        public void setControlBarVisibleAnimate(final boolean visible, final ControlBarAnimationListener listener) {
            final int newDirection = visible ? 1 : -1;
            if (mControlAnimationDirection == newDirection) return;
            if (mCurrentControlAnimation != null && mControlAnimationDirection != 0) {
                mCurrentControlAnimation.cancel();
                mCurrentControlAnimation = null;
                mControlAnimationDirection = newDirection;
            }
            final ObjectAnimator animator;
            final float offset = mActivity.getControlBarOffset();
            if (visible) {
                if (offset >= 1) return;
                animator = ObjectAnimator.ofFloat(mActivity, ControlBarOffsetProperty.SINGLETON, offset, 1);
            } else {
                if (offset <= 0) return;
                animator = ObjectAnimator.ofFloat(mActivity, ControlBarOffsetProperty.SINGLETON, offset, 0);
            }
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mControlAnimationDirection = 0;
                    mCurrentControlAnimation = null;
                    if (listener != null) {
                        listener.onControlBarVisibleAnimationFinish(visible);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    mControlAnimationDirection = 0;
                    mCurrentControlAnimation = null;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.setDuration(DURATION);
            animator.start();
            mCurrentControlAnimation = animator;
            mControlAnimationDirection = newDirection;
        }
    }
}