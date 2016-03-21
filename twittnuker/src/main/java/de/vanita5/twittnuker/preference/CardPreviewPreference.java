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

package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.adapter.DummyItemAdapter;
import de.vanita5.twittnuker.graphic.like.LikeAnimationDrawable;
import de.vanita5.twittnuker.view.holder.StatusViewHolder;
import de.vanita5.twittnuker.view.holder.iface.IStatusViewHolder;

public class CardPreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

    private StatusViewHolder mHolder;
    private boolean mCompactModeChanged;
    private DummyItemAdapter mAdapter;

    public CardPreviewPreference(final Context context) {
        this(context, null);
    }

    public CardPreviewPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardPreviewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        setLayoutResources(preferences);
        preferences.registerOnSharedPreferenceChangeListener(this);
        mAdapter = new DummyItemAdapter(context);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        if (mHolder == null) return;
        if (KEY_COMPACT_CARDS.equals(key)) {
            setLayoutResources(preferences);
        }
        mAdapter.updateOptions();
        notifyChanged();
    }

    protected void setLayoutResources(SharedPreferences preferences) {
        if (preferences.getBoolean(KEY_COMPACT_CARDS, false)) {
            setLayoutResource(R.layout.layout_preferences_card_preview_compact);
            mHolder = null;
        } else {
            setLayoutResource(R.layout.layout_preferences_card_preview);
            mHolder = null;
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (mHolder == null) {
            mHolder = new StatusViewHolder(mAdapter, holder.itemView);
        }
        mCompactModeChanged = false;
        mHolder.setupViewOptions();
        mHolder.displaySampleStatus();
        mHolder.setStatusClickListener(new IStatusViewHolder.SimpleStatusClickListener() {
            @Override
            public void onItemActionClick(RecyclerView.ViewHolder holder, int id, int position) {
                if (id == R.id.favorite) {
                    ((StatusViewHolder) holder).playLikeAnimation(new LikeAnimationDrawable.OnLikedListener() {
                        @Override
                        public boolean onLiked() {
                            return false;
                        }
                    });
                }
            }
        });
        super.onBindViewHolder(holder);
    }

}