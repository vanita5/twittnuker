/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2017 vanita5 <mail@vanit.as>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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
package de.vanita5.twittnuker.library.fanfou;

import de.vanita5.twittnuker.library.fanfou.api.BlocksResources;
import de.vanita5.twittnuker.library.fanfou.api.DirectMessagesResources;
import de.vanita5.twittnuker.library.fanfou.api.FavoritesResources;
import de.vanita5.twittnuker.library.fanfou.api.FriendshipsResources;
import de.vanita5.twittnuker.library.fanfou.api.PhotosResources;
import de.vanita5.twittnuker.library.fanfou.api.SearchResources;
import de.vanita5.twittnuker.library.fanfou.api.StatusesResources;
import de.vanita5.twittnuker.library.fanfou.api.TrendsResources;
import de.vanita5.twittnuker.library.fanfou.api.UsersResources;

public interface Fanfou extends StatusesResources, SearchResources, UsersResources, PhotosResources,
        FriendshipsResources, BlocksResources, FavoritesResources, DirectMessagesResources,
        TrendsResources {
}