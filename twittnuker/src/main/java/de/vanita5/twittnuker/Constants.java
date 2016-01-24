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

package de.vanita5.twittnuker;

/**
 * Constants requires full application to build or useless for other
 * applications
 *
 * @author mariotaku
 */
public interface Constants extends TwittnukerConstants {

    String DATABASES_NAME = "twittnuker.sqlite";
    int DATABASES_VERSION = 118;

    int MENU_GROUP_STATUS_SHARE = 20;

    int LINK_ID_STATUS = 1;
    int LINK_ID_USER = 2;
    int LINK_ID_USER_TIMELINE = 3;
    int LINK_ID_USER_FAVORITES = 4;
    int LINK_ID_USER_FOLLOWERS = 5;
    int LINK_ID_USER_FRIENDS = 6;
    int LINK_ID_USER_BLOCKS = 7;
    int LINK_ID_USER_MEDIA_TIMELINE = 8;
    int LINK_ID_DIRECT_MESSAGES_CONVERSATION = 9;
    int LINK_ID_USER_LIST = 10;
    int LINK_ID_USER_LISTS = 11;
    int LINK_ID_USER_LIST_TIMELINE = 12;
    int LINK_ID_USER_LIST_MEMBERS = 13;
    int LINK_ID_USER_LIST_SUBSCRIBERS = 14;
    int LINK_ID_USER_LIST_MEMBERSHIPS = 15;
    int LINK_ID_SAVED_SEARCHES = 19;
    int LINK_ID_USER_MENTIONS = 21;
    int LINK_ID_INCOMING_FRIENDSHIPS = 22;
    int LINK_ID_USERS = 23;
    int LINK_ID_STATUSES = 24;
    int LINK_ID_STATUS_RETWEETERS = 25;
    int LINK_ID_STATUS_REPLIES = 26;
    int LINK_ID_STATUS_FAVORITERS = 27;
    int LINK_ID_SEARCH = 28;
    int LINK_ID_MUTES_USERS = 41;
    int LINK_ID_MAP = 51;
    int LINK_ID_SCHEDULED_STATUSES = 61;
    int LINK_ID_ACCOUNTS = 101;
    int LINK_ID_DRAFTS = 102;
    int LINK_ID_FILTERS = 103;
    int LINK_ID_PROFILE_EDITOR = 104;

    String DIR_NAME_IMAGE_CACHE = "image_cache";
    String DIR_NAME_FULL_IMAGE_CACHE = "full_image_cache";

    String FRAGMENT_TAG_API_UPGRADE_NOTICE = "api_upgrade_notice";

    String TWIDERE_PREVIEW_NAME = "Twittnuker Project";
    String TWIDERE_PREVIEW_SCREEN_NAME = "twittnuker";
    String TWIDERE_PREVIEW_TEXT_HTML = "Twittnuker is an open source twitter client for Android, see <a href='https://github.com/vanita5/twittnuker'>https://github.com/vanita5/twittnuker<a/>";
    String TWIDERE_PREVIEW_SOURCE = "Twittnuker";

    int MATERIAL_DARK = 0xff212121;
    int MATERIAL_LIGHT = 0xffbdbdbd;

    int[] PRESET_COLORS = {R.color.material_dark, R.color.material_light, R.color.material_red, R.color.material_pink,
            R.color.material_purple, R.color.material_deep_purple, R.color.material_indigo,
            R.color.material_blue, R.color.material_light_blue, R.color.material_cyan,
            R.color.material_teal, R.color.material_green, R.color.material_light_green,
            R.color.material_lime, R.color.material_yellow, R.color.material_amber,
            R.color.material_orange, R.color.material_deep_orange};

    String READ_POSITION_TAG_ACTIVITIES_ABOUT_ME = "activities_about_me";

}
