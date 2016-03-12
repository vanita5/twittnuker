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

package de.vanita5.twittnuker.loader.support;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import de.vanita5.twittnuker.api.twitter.TwitterException;
import de.vanita5.twittnuker.constant.IntentConstants;
import de.vanita5.twittnuker.model.AccountKey;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.util.DataStoreUtils;

import static de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT;
import static de.vanita5.twittnuker.util.Utils.findStatus;

public class ParcelableStatusLoader extends AsyncTaskLoader<SingleResponse<ParcelableStatus>> {

    private final boolean mOmitIntentExtra;
    private final Bundle mExtras;
    private final AccountKey mAccountId;
    private final long mStatusId;

    public ParcelableStatusLoader(final Context context, final boolean omitIntentExtra, final Bundle extras,
                                  final AccountKey accountId, final long statusId) {
        super(context);
        mOmitIntentExtra = omitIntentExtra;
        mExtras = extras;
        mAccountId = accountId;
        mStatusId = statusId;
    }

    @Override
    public SingleResponse<ParcelableStatus> loadInBackground() {
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableStatus cache = mExtras.getParcelable(IntentConstants.EXTRA_STATUS);
            if (cache != null) {
                final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(cache);
                final Bundle extras = response.getExtras();
                extras.putParcelable(EXTRA_ACCOUNT, DataStoreUtils.getCredentials(getContext(), mAccountId));
                return response;
            }
        }
        try {
            final ParcelableStatus status = findStatus(getContext(), mAccountId, mStatusId);
            final ParcelableCredentials credentials = DataStoreUtils.getCredentials(getContext(), mAccountId);
            final SingleResponse<ParcelableStatus> response = SingleResponse.getInstance(status);
            final Bundle extras = response.getExtras();
            extras.putParcelable(EXTRA_ACCOUNT, credentials);
            return response;
        } catch (final TwitterException e) {
            return SingleResponse.getInstance(e);
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}