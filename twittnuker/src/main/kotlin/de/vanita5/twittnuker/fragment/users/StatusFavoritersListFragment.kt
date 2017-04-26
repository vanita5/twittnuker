/*
 *          Twittnuker - Twitter client for Android
 *
 *  Copyright 2013-2017 vanita5 <mail@vanit.as>
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 *  Copyright 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.vanita5.twittnuker.fragment.users

import android.content.Context
import android.os.Bundle
import de.vanita5.twittnuker.adapter.ParcelableUsersAdapter
import de.vanita5.twittnuker.adapter.iface.ILoadMoreSupportAdapter
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_ACCOUNT_KEY
import de.vanita5.twittnuker.constant.IntentConstants.EXTRA_STATUS_ID
import de.vanita5.twittnuker.fragment.ParcelableUsersFragment
import de.vanita5.twittnuker.loader.users.AbsRequestUsersLoader
import de.vanita5.twittnuker.loader.users.StatusFavoritersLoader
import de.vanita5.twittnuker.model.UserKey

class StatusFavoritersListFragment : ParcelableUsersFragment() {

    override fun onCreateUsersLoader(context: Context, args: Bundle, fromUser: Boolean):
            AbsRequestUsersLoader {
        val accountKey = args.getParcelable<UserKey>(EXTRA_ACCOUNT_KEY)
        val statusId = args.getString(EXTRA_STATUS_ID)
        return StatusFavoritersLoader(context, accountKey, statusId, adapter.getData(), false)
    }

    override fun onCreateAdapter(context: Context): ParcelableUsersAdapter {
        return super.onCreateAdapter(context).apply {
            loadMoreSupportedPosition = ILoadMoreSupportAdapter.NONE
        }
    }

}