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

package de.vanita5.twittnuker.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.adapter.iface.IBaseAdapter;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.MultiSelectManager;
import de.vanita5.twittnuker.util.OnLinkClickHandler;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.TwidereLinkify;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import java.util.Collection;

import javax.inject.Inject;

public class BaseArrayAdapter<T> extends ArrayAdapter<T> implements Constants, IBaseAdapter,
        OnSharedPreferenceChangeListener {

    private final TwidereLinkify mLinkify;
    @Inject
    protected UserColorNameManager mUserColorNameManager;

    private float mTextSize;
    private int mLinkHighlightOption;

    private boolean mDisplayProfileImage, mDisplayNameFirst, mShowAccountColor;

    private final SharedPreferences mColorPrefs;
    @Inject
    protected MediaLoaderWrapper mImageLoader;
    @Inject
    protected MultiSelectManager mMultiSelectManager;
    @Inject
    protected SharedPreferencesWrapper mPreferences;

    public BaseArrayAdapter(final Context context, final int layoutRes) {
        this(context, layoutRes, null);
    }

    public BaseArrayAdapter(final Context context, final int layoutRes, final Collection<? extends T> collection) {
        super(context, layoutRes, collection);
        //noinspection unchecked
        GeneralComponentHelper.build(context).inject((BaseArrayAdapter<Object>) this);
        final TwittnukerApplication app = TwittnukerApplication.getInstance(context);
        mLinkify = new TwidereLinkify(new OnLinkClickHandler(context, mMultiSelectManager, mPreferences));
        mColorPrefs = context.getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mColorPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public MediaLoaderWrapper getImageLoader() {
        return mImageLoader;
    }

    @Override
    public final int getLinkHighlightOption() {
        return mLinkHighlightOption;
    }

    public final TwidereLinkify getLinkify() {
        return mLinkify;
    }

    @Override
    public final float getTextSize() {
        return mTextSize;
    }

    @Override
    public final boolean isDisplayNameFirst() {
        return mDisplayNameFirst;
    }

    @Override
    public final boolean isProfileImageDisplayed() {
        return mDisplayProfileImage;
    }

    @Override
    public final boolean isShowAccountColor() {
        return mShowAccountColor;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        if (KEY_DISPLAY_PROFILE_IMAGE.equals(key) || KEY_MEDIA_PREVIEW_STYLE.equals(key)
                || KEY_DISPLAY_SENSITIVE_CONTENTS.equals(key)) {
            notifyDataSetChanged();
        }
    }

    @Override
    public final void setDisplayNameFirst(final boolean nameFirst) {
        mDisplayNameFirst = nameFirst;
    }

    @Override
    public final void setDisplayProfileImage(final boolean display) {
        mDisplayProfileImage = display;
    }

    @Override
    public final void setLinkHighlightOption(final String option) {
        final int optionInt = Utils.getLinkHighlightingStyleInt(option);
        mLinkify.setHighlightOption(optionInt);
        if (optionInt == mLinkHighlightOption) return;
        mLinkHighlightOption = optionInt;
    }

    @Override
    public final void setShowAccountColor(final boolean show) {
        mShowAccountColor = show;
    }

    @Override
    public final void setTextSize(final float textSize) {
        mTextSize = textSize;
    }

}