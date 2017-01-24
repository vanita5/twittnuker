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
    int DATABASES_VERSION = 161;

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
    int LINK_ID_GROUP = 16;
    int LINK_ID_USER_GROUPS = 17;
    int LINK_ID_SAVED_SEARCHES = 19;
    int LINK_ID_ITEMS = 20;
    int LINK_ID_USER_MENTIONS = 21;
    int LINK_ID_INCOMING_FRIENDSHIPS = 22;
    int LINK_ID_STATUS_RETWEETERS = 25;
    int LINK_ID_STATUS_FAVORITERS = 27;
    int LINK_ID_SEARCH = 28;
    int LINK_ID_DIRECT_MESSAGES = 29;
    int LINK_ID_INTERACTIONS = 30;
    int LINK_ID_PUBLIC_TIMELINE = 31;
    int LINK_ID_MUTES_USERS = 41;
    int LINK_ID_MAP = 51;
    int LINK_ID_SCHEDULED_STATUSES = 61;
    int LINK_ID_ACCOUNTS = 101;
    int LINK_ID_DRAFTS = 102;
    int LINK_ID_FILTERS = 110;
    int LINK_ID_FILTERS_IMPORT_BLOCKS = 111;
    int LINK_ID_FILTERS_IMPORT_MUTES = 112;
    int LINK_ID_FILTERS_SUBSCRIPTIONS = 113;
    int LINK_ID_PROFILE_EDITOR = 121;

    String TWITTNUKER_PREVIEW_NAME = "Twittnuker Project";
    String TWITTNUKER_PREVIEW_SCREEN_NAME = "twittnuker";
    String TWITTNUKER_PREVIEW_TEXT_HTML = "Twittnuker is an open source twitter client for Android, see <a href='https://github.com/vanita5/twittnuker'>https://github.com/vanita5/twittnuker</a>";
    String TWITTNUKER_PREVIEW_TEXT_UNESCAPED = "Twittnuker is an open source twitter client for Android, see github.com/vanita5/&#8230;";
    String TWITTNUKER_PREVIEW_SOURCE = "Twittnuker";
    String TWITTNUKER_PREVIEW_LOCATION = "Freiburg, Germany";

    String EXTRA_PRODUCT_TYPE = "product_type";

    int MATERIAL_DARK = 0xff212121;
    int MATERIAL_LIGHT = 0xffbdbdbd;

    int[] PRESET_COLORS = {R.color.material_dark, R.color.material_light, R.color.material_red, R.color.material_pink,
            R.color.material_purple, R.color.material_deep_purple, R.color.material_indigo,
            R.color.material_blue, R.color.material_light_blue, R.color.material_cyan,
            R.color.material_teal, R.color.material_green, R.color.material_light_green,
            R.color.material_lime, R.color.material_yellow, R.color.material_amber,
            R.color.material_orange, R.color.material_deep_orange};

    @SuppressWarnings("SpellCheckingInspection")
    String GOOGLE_PLAY_LICENCING_PUBKEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs2KZ58y8Z56KchEP2iQHvuznrZAyDf9ULm+L0C2PZKcZjHGxC3XbXH9VC9qVV1GUcPJEIXht0VanUGYPHbCQDVnRPQuNyrF4rOLB5qLEh71IxnlK0OjnKGXRolSTldsZUhC1ja8n5MI0bi3r1oRduM0fDC4E+piIrfRZBjPm6p9OLckwgzz+rulYFErmQAoPhmUr4AvV3WgYNm0Lof+eLpZVpGfxqxOpmt3fMe30/nEnvLVHdOU1wNix9hq94uLrzHVLBuXTT7v99QnX/HB5dztnI54lGK7GvmwCTfrjcgdyf63D4+r1eF/E3Bx2kp/ZtezE0vWGda6bXgecdlJ/LQIDAQAB";

    @SuppressWarnings("SpellCheckingInspection")
    String DROPBOX_APP_KEY = "cm68n97voadq7b8";

}
