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

package de.vanita5.twittnuker.task;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.otto.Bus;

import org.mariotaku.abstask.library.AbstractTask;
import de.vanita5.twittnuker.library.MicroBlog;
import de.vanita5.twittnuker.library.MicroBlogException;
import de.vanita5.twittnuker.library.twitter.model.User;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.model.ParcelableUser;
import de.vanita5.twittnuker.model.SingleResponse;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.model.message.ProfileUpdatedEvent;
import de.vanita5.twittnuker.model.util.ParcelableUserUtils;
import de.vanita5.twittnuker.util.MicroBlogAPIFactory;
import de.vanita5.twittnuker.util.TwitterWrapper;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.util.dagger.GeneralComponentHelper;

import java.io.IOException;

import javax.inject.Inject;

public class UpdateProfileBackgroundImageTask<ResultHandler> extends AbstractTask<Object,
        SingleResponse<ParcelableUser>, ResultHandler> implements Constants {

    @Inject
    protected Bus mBus;

    private final UserKey mAccountKey;
    private final Uri mImageUri;
    private boolean mTile;
    private final boolean mDeleteImage;
    private final Context mContext;

    public UpdateProfileBackgroundImageTask(final Context context, final UserKey accountKey,
                                            final Uri imageUri, final boolean tile,
                                            final boolean deleteImage) {
        //noinspection unchecked
        GeneralComponentHelper.build(context).inject((UpdateProfileBackgroundImageTask<Object>) this);
        mContext = context;
        mAccountKey = accountKey;
        mImageUri = imageUri;
        mDeleteImage = deleteImage;
        mTile = tile;
    }

    @Override
    protected void afterExecute(ResultHandler callback, SingleResponse<ParcelableUser> result) {
        super.afterExecute(callback, result);
        if (result.hasData()) {
            Utils.showOkMessage(mContext, R.string.profile_banner_image_updated, false);
            mBus.post(new ProfileUpdatedEvent(result.getData()));
        } else {
            Utils.showErrorMessage(mContext, R.string.action_updating_profile_background_image,
                    result.getException(),
                    true);
        }
    }

    @Override
    protected SingleResponse<ParcelableUser> doLongOperation(final Object params) {
        try {
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(mContext, mAccountKey,
                    true);
            TwitterWrapper.updateProfileBackgroundImage(mContext, twitter, mImageUri, mTile,
                    mDeleteImage);
            // Wait for 5 seconds, see
            // https://dev.twitter.com/docs/api/1.1/post/account/update_profile_image
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                Log.w(LOGTAG, e);
            }
            final User user = twitter.verifyCredentials();
            return SingleResponse.Companion.getInstance(ParcelableUserUtils.fromUser(user, mAccountKey));
        } catch (MicroBlogException | IOException e) {
            return SingleResponse.Companion.getInstance(e);
        }
    }


}