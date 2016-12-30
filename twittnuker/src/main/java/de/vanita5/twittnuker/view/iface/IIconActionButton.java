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

package de.vanita5.twittnuker.view.iface;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.internal.ChameleonTypedArray;
import de.vanita5.twittnuker.R;

public interface IIconActionButton extends ChameleonView {
    @ColorInt
    int getDefaultColor();

    @ColorInt
    int getActivatedColor();

    @ColorInt
    int getDisabledColor();

    void setDefaultColor(@ColorInt int defaultColor);

    void setActivatedColor(@ColorInt int activatedColor);

    void setDisabledColor(@ColorInt int disabledColor);


    class Appearance implements ChameleonView.Appearance {
        @ColorInt
        private int defaultColor, activatedColor, disabledColor;

        public int getActivatedColor() {
            return activatedColor;
        }

        public void setActivatedColor(int activatedColor) {
            this.activatedColor = activatedColor;
        }

        public int getDefaultColor() {
            return defaultColor;
        }

        public void setDefaultColor(int defaultColor) {
            this.defaultColor = defaultColor;
        }

        public int getDisabledColor() {
            return disabledColor;
        }

        public void setDisabledColor(int disabledColor) {
            this.disabledColor = disabledColor;
        }

        public static Appearance create(Context context, AttributeSet attributeSet, Chameleon.Theme theme) {
            Appearance appearance = new Appearance();
            ChameleonTypedArray a = ChameleonTypedArray.obtain(context, attributeSet, R.styleable.IconActionButton, theme);
            appearance.setDefaultColor(a.getColor(R.styleable.IconActionButton_iabColor, theme.getColorForeground()));
            appearance.setActivatedColor(a.getColor(R.styleable.IconActionButton_iabActivatedColor, theme.getColorAccent()));
            appearance.setActivatedColor(a.getColor(R.styleable.IconActionButton_iabDisabledColor, theme.getTextColorSecondary()));
            a.recycle();
            return appearance;
        }

        public static void apply(IIconActionButton view, Appearance appearance) {
            view.setDefaultColor(appearance.getDefaultColor());
            view.setActivatedColor(appearance.getActivatedColor());
            view.setDisabledColor(appearance.getDisabledColor());
        }
    }
}