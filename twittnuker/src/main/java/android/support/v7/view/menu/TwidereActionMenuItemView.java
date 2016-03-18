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

package android.support.v7.view.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.view.menu.MenuItemImpl;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.inflation.ViewInterface;
import com.afollestad.appthemeengine.util.ATEUtil;
import com.afollestad.appthemeengine.util.TintHelper;
import com.afollestad.appthemeengine.viewprocessors.ViewProcessor;

import de.vanita5.twittnuker.util.ThemeUtils;

public class TwidereActionMenuItemView extends ActionMenuItemView implements ViewInterface {

    public TwidereActionMenuItemView(Context context) {
        super(context);
        init(context, null);
    }

    public TwidereActionMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public TwidereActionMenuItemView(Context context, AttributeSet attrs, @Nullable ATEActivity keyContext) {
        super(context, attrs);
        init(context, keyContext);
    }

    private String mKey;
    private int mTintColor;
    private Drawable mIcon;
    private boolean mCheckedActionView;

    private void init(Context context, @Nullable ATEActivity keyContext) {
        if (keyContext == null && context instanceof ATEActivity)
            keyContext = (ATEActivity) context;
        mKey = null;
        if (keyContext != null)
            mKey = keyContext.getATEKey();

        if (mIcon != null)
            setIcon(mIcon); // invalidates initial icon tint
        else invalidateTintColor();

        ATE.themeView(context, this, mKey);
        setTextColor(mTintColor); // sets menu item text color
    }

    private void invalidateTintColor() {
        final int colorBackground = ATEUtil.resolveColor(getContext(), android.R.attr.colorBackground);
        mTintColor = ThemeUtils.getActionIconColor(getContext(), colorBackground);
    }

    @Override
    public void setIcon(Drawable icon) {
        invalidateTintColor();
        mIcon = TintHelper.createTintedDrawable(icon, mTintColor);
        super.setIcon(mIcon);
        invalidateActionView();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        invalidateActionView();
    }

    @SuppressWarnings("unchecked")
    private void invalidateActionView() {
        if (mCheckedActionView) return;
        mCheckedActionView = true;
        View actionView = getActionView();
        if (actionView != null) {
            ViewProcessor processor = ATE.getViewProcessor(actionView.getClass());
            if (processor != null)
                processor.process(getContext(), mKey, actionView, null);
        }
    }

    @Nullable
    private View getActionView() {
        final MenuItemImpl menuImpl = getItemData();
        if (menuImpl == null) return null;
        return menuImpl.getActionView();
    }

    @Override
    public boolean setsStatusBarColor() {
        return false;
    }

    @Override
    public boolean setsToolbarColor() {
        return false;
    }
}