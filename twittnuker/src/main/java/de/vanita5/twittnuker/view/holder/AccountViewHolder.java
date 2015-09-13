/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.view.holder;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.view.ColorLabelRelativeLayout;

public class AccountViewHolder {

    public final ImageView profileImage;
    public final TextView name, screenName;
    public final CompoundButton toggle;
    private final ColorLabelRelativeLayout content;
    private final View dragHandle;

    public AccountViewHolder(final View view) {
        content = (ColorLabelRelativeLayout) view;
        name = (TextView) view.findViewById(android.R.id.text1);
        screenName = (TextView) view.findViewById(android.R.id.text2);
        profileImage = (ImageView) view.findViewById(android.R.id.icon);
        toggle = (CompoundButton) view.findViewById(android.R.id.toggle);
        dragHandle = view.findViewById(R.id.drag_handle);
    }

    public void setAccountColor(final int color) {
        content.drawEnd(color);
    }

    public void setSortEnabled(boolean enabled) {
        dragHandle.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }
}