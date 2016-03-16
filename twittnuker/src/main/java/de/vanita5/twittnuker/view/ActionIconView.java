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

package de.vanita5.twittnuker.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.util.ThemeUtils;

public class ActionIconView extends AppCompatImageView {

    @ColorInt
    private int mDefaultColor, mActivatedColor, mDisabledColor;

    public ActionIconView(Context context) {
        this(context, null);
    }

    public ActionIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IconActionButton,
                R.attr.cardActionButtonStyle, R.style.Widget_CardActionButton);
        mDefaultColor = a.getColor(R.styleable.IconActionButton_iabColor, 0);
        mActivatedColor = a.getColor(R.styleable.IconActionButton_iabActivatedColor, 0);
        mDisabledColor = a.getColor(R.styleable.IconActionButton_iabDisabledColor, 0);
        a.recycle();
        updateColorFilter();
    }

    @ColorInt
    public int getDefaultColor() {
        if (mDefaultColor == 0) {
            // Return inverse color for background tint
            ColorStateList color = ViewCompat.getBackgroundTintList(this);
            if (color != null) {
                final int currentColor = color.getColorForState(getDrawableState(), 0);
                return ThemeUtils.getContrastColor(currentColor, Color.BLACK, Color.WHITE);
            }
        }
        return mDefaultColor;
    }

    @ColorInt
    public int getActivatedColor() {
        if (mActivatedColor != 0) return mActivatedColor;
        return getDefaultColor();
    }

    @ColorInt
    public int getDisabledColor() {
        if (mDisabledColor != 0) return mDisabledColor;
        return getDefaultColor();
    }

    public void setDefaultColor(@ColorInt int defaultColor) {
        mDefaultColor = defaultColor;
        updateColorFilter();
    }

    public void setActivatedColor(@ColorInt int activatedColor) {
        mActivatedColor = activatedColor;
        updateColorFilter();
    }

    public void setDisabledColor(@ColorInt int disabledColor) {
        mDisabledColor = disabledColor;
        updateColorFilter();
    }

    @Override
    public void setActivated(boolean activated) {
        super.setActivated(activated);
        updateColorFilter();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateColorFilter();
    }

    private void updateColorFilter() {
        if (isActivated()) {
            setColorFilter(getActivatedColor());
        } else if (isEnabled()) {
            setColorFilter(getDefaultColor());
        } else {
            setColorFilter(getDisabledColor());
        }
    }
}