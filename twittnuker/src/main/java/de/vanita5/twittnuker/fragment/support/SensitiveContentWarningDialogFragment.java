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

package de.vanita5.twittnuker.fragment.support;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.ParcelableMedia;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.util.IntentUtils;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;

public class SensitiveContentWarningDialogFragment extends BaseSupportDialogFragment implements
        DialogInterface.OnClickListener {

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE: {
                final Context context = getActivity();
                final Bundle args = getArguments();
                if (args == null || context == null) return;
                final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
                final ParcelableMedia current = args.getParcelable(EXTRA_CURRENT_MEDIA);
                final ParcelableStatus status = args.getParcelable(EXTRA_STATUS);
                final Bundle option = args.getBundle(EXTRA_ACTIVITY_OPTIONS);
                final boolean newDocument = args.getBoolean(EXTRA_NEW_DOCUMENT);
                final ParcelableMedia[] media = Utils.newParcelableArray(args.getParcelableArray(EXTRA_MEDIA),
                        ParcelableMedia.CREATOR);
                IntentUtils.openMediaDirectly(context, accountKey, status, null, current, media,
                        option, newDocument);
                break;
            }
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context wrapped = ThemeUtils.getDialogThemedContext(getActivity());
        final AlertDialog.Builder builder = new AlertDialog.Builder(wrapped);
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setMessage(R.string.sensitive_content_warning);
        builder.setPositiveButton(android.R.string.ok, this);
        builder.setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }

}