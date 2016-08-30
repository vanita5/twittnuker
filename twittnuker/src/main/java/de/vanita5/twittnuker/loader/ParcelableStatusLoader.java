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

package de.vanita5.twittnuker.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.ErrorInfo;
import de.vanita5.twittnuker.constant.IntentConstants;
import de.vanita5.twittnuker.model.ParcelableCredentials;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.util.ParcelableCredentialsUtils;
import de.vanita5.twittnuker.model.util.ParcelableStatusUtils;
import de.vanita5.twittnuker.util.DataStoreUtils;
import de.vanita5.twittnuker.util.UserColorNameManager;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import javax.inject.Inject;

import static de.vanita5.twittnuker.util.Utils.findStatus;

public class ParcelableStatusLoader extends AsyncTaskLoader<SingleResponse<ParcelableStatus>>
        implements Constants {

    private final boolean mOmitIntentExtra;
    private final Bundle mExtras;
    @Nullable
    private final UserKey mAccountKey;
    @Nullable
    private final String mStatusId;

    @Inject
    UserColorNameManager mUserColorNameManager;

    public ParcelableStatusLoader(final Context context, final boolean omitIntentExtra, final Bundle extras,
                                  @Nullable final UserKey accountKey,
                                  @Nullable final String statusId) {
        super(context);
        GeneralComponentHelper.build(context).inject(this);
        mOmitIntentExtra = omitIntentExtra;
        mExtras = extras;
        mAccountKey = accountKey;
        mStatusId = statusId;
    }

    @Override
    public SingleResponse<ParcelableStatus> loadInBackground() {
        if (mAccountKey == null || mStatusId == null) return SingleResponse.Companion.getInstance();
        if (!mOmitIntentExtra && mExtras != null) {
            final ParcelableStatus cache = mExtras.getParcelable(IntentConstants.EXTRA_STATUS);
            if (cache != null) {
                final SingleResponse<ParcelableStatus> response = SingleResponse.Companion.getInstance(cache);
                final Bundle extras = response.getExtras();
                extras.putParcelable(EXTRA_ACCOUNT, ParcelableCredentialsUtils.getCredentials(getContext(), mAccountKey));
                return response;
            }
        }
        try {
            final ParcelableCredentials credentials = ParcelableCredentialsUtils.getCredentials(getContext(), mAccountKey);
            if (credentials == null) return SingleResponse.Companion.getInstance();
            final ParcelableStatus status = findStatus(getContext(), mAccountKey, mStatusId);
            ParcelableStatusUtils.updateExtraInformation(status, credentials, mUserColorNameManager);
            final SingleResponse<ParcelableStatus> response = SingleResponse.Companion.getInstance(status);
            final Bundle extras = response.getExtras();
            extras.putParcelable(EXTRA_ACCOUNT, credentials);
            return response;
        } catch (final MicroBlogException e) {
            if (e.getErrorCode() == ErrorInfo.STATUS_NOT_FOUND) {
                // Delete all deleted status
                final ContentResolver cr = getContext().getContentResolver();
                DataStoreUtils.deleteStatus(cr, mAccountKey,
                        mStatusId, null);
                DataStoreUtils.deleteActivityStatus(cr, mAccountKey, mStatusId, null);
            }
            return SingleResponse.Companion.getInstance(e);
        }
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

}