/*
 *          Twittnuker - Twitter client for Android
 *
 *          This program incorporates a modified version of
 *          Twidere - Twitter client for Android
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.vanita5.twittnuker;

import android.content.ContentResolver;

import de.vanita5.twittnuker.constant.CompatibilityConstants;
import de.vanita5.twittnuker.constant.IntentConstants;
import de.vanita5.twittnuker.constant.SharedPreferenceConstants;

/**
 * Public constants for Twittnuker
 *
 * @author mariotaku
 */
public interface TwittnukerConstants extends SharedPreferenceConstants,
        IntentConstants, SecretConstants, CompatibilityConstants {

    String TWITTNUKER_APP_NAME = "Twittnuker";
    String TWITTNUKER_PROJECT_URL = "https://github.com/vanita5/twittnuker";
    String TWITTNUKER_PROJECT_EMAIL = "mail@twittnuker.org";
    String TWITTNUKER_PACKAGE_NAME = "de.vanita5.twittnuker";

    String ACCOUNT_TYPE = "de.vanita5.twittnuker.account";
    String ACCOUNT_AUTH_TOKEN_TYPE = "de.vanita5.twittnuker.account.token";
    String ACCOUNT_USER_DATA_KEY = "key";
    String ACCOUNT_USER_DATA_TYPE = "type";
    String ACCOUNT_USER_DATA_CREDS_TYPE = "creds_type";
    String ACCOUNT_USER_DATA_ACTIVATED = "activated";
    String ACCOUNT_USER_DATA_USER = "user";
    String ACCOUNT_USER_DATA_EXTRAS = "extras";
    String ACCOUNT_USER_DATA_COLOR = "color";
    String ACCOUNT_USER_DATA_POSITION = "position";

    String LOGTAG = TWITTNUKER_APP_NAME;

    String USER_COLOR_PREFERENCES_NAME = "user_colors";
    String HOST_MAPPING_PREFERENCES_NAME = "host_mapping";
    String MESSAGE_DRAFTS_PREFERENCES_NAME = "message_drafts";
    String SHARED_PREFERENCES_NAME = "preferences";
    String SYNC_PREFERENCES_NAME = "sync_preferences";
    String TIMELINE_POSITIONS_PREFERENCES_NAME = "timeline_positions";
    String ACCOUNT_PREFERENCES_NAME_PREFIX = "account_preferences_";
    String KEYBOARD_SHORTCUTS_PREFERENCES_NAME = "keyboard_shortcuts_preferences";
    String ETAG_CACHE_PREFERENCES_NAME = "etag_cache";

    String DEFAULT_TWITTER_API_URL_FORMAT = "https://[DOMAIN.]twitter.com/";

    String SCHEME_HTTP = "http";
    String SCHEME_HTTPS = "https";
    String SCHEME_CONTENT = ContentResolver.SCHEME_CONTENT;
    String SCHEME_TWITTNUKER = "twittnuker";
    String SCHEME_TWITTNUKER_SETTINGS = "twittnuker.settings";
    String SCHEME_DATA = "data";

    String PROTOCOL_HTTP = SCHEME_HTTP + "://";
    String PROTOCOL_HTTPS = SCHEME_HTTPS + "://";
    String PROTOCOL_CONTENT = SCHEME_CONTENT + "://";
    String PROTOCOL_TWITTNUKER = SCHEME_TWITTNUKER + "://";

    String AUTHORITY_TWITTNUKER_SHARE = "twittnuker.share";
    String AUTHORITY_TWITTNUKER_CACHE = "twittnuker.cache";

    String AUTHORITY_USER = "user";
    String AUTHORITY_ITEMS = "items";
    String AUTHORITY_USER_TIMELINE = "user_timeline";
    String AUTHORITY_USER_MEDIA_TIMELINE = "user_media_timeline";
    String AUTHORITY_USER_FAVORITES = "user_favorites";
    String AUTHORITY_USER_FOLLOWERS = "user_followers";
    String AUTHORITY_USER_FRIENDS = "user_friends";
    String AUTHORITY_USER_BLOCKS = "user_blocks";
    String AUTHORITY_STATUS = "status";
    String AUTHORITY_PUBLIC_TIMELINE = "public_timeline";
    String AUTHORITY_MESSAGES = "direct_messages";
    String AUTHORITY_SEARCH = "search";
    String AUTHORITY_MAP = "map";
    String AUTHORITY_USER_LIST = "user_list";
    String AUTHORITY_USER_LIST_TIMELINE = "user_list_timeline";
    String AUTHORITY_GROUP = "group";
    String AUTHORITY_GROUP_TIMELINE = "group_timeline";
    String AUTHORITY_USER_LIST_MEMBERS = "user_list_members";
    String AUTHORITY_USER_LIST_SUBSCRIBERS = "user_list_subscribers";
    String AUTHORITY_USER_LIST_MEMBERSHIPS = "user_list_memberships";
    String AUTHORITY_USER_LISTS = "user_lists";
    String AUTHORITY_USER_GROUPS = "user_groups";
    String AUTHORITY_USERS_RETWEETED_STATUS = "users_retweeted_status";
    String AUTHORITY_SAVED_SEARCHES = "saved_searches";
    String AUTHORITY_SEARCH_USERS = "search_users";
    String AUTHORITY_SEARCH_TWEETS = "search_tweets";
    String AUTHORITY_TRENDS = "trends";
    String AUTHORITY_USER_MENTIONS = "user_mentions";
    String AUTHORITY_INCOMING_FRIENDSHIPS = "incoming_friendships";
    String AUTHORITY_STATUS_RETWEETERS = "status_retweeters";
    String AUTHORITY_STATUS_FAVORITERS = "status_favoriters";
    String AUTHORITY_RETWEETS_OF_ME = "retweets_of_me";
    String AUTHORITY_MUTES_USERS = "mutes_users";
    String AUTHORITY_INTERACTIONS = "interactions";
    String AUTHORITY_ACCOUNTS = "accounts";
    String AUTHORITY_DRAFTS = "drafts";
    String AUTHORITY_FILTERS = "filters";
    String AUTHORITY_PROFILE_EDITOR = "profile_editor";
    String AUTHORITY_QUOTE = "quote";

    String PATH_FILTERS_IMPORT_BLOCKS = "import/blocks";
    String PATH_FILTERS_IMPORT_MUTES = "import/mutes";
    String PATH_FILTERS_SUBSCRIPTIONS = "subscriptions";
    String PATH_FILTERS_SUBSCRIPTIONS_ADD = "subscriptions/add";

    String PATH_MESSAGES_CONVERSATION = "conversation";
    String PATH_MESSAGES_CONVERSATION_NEW = "conversation/new";
    String PATH_MESSAGES_CONVERSATION_INFO = "conversation/info";

    String QUERY_PARAM_ACCOUNT_KEY = "account_key";
    String QUERY_PARAM_ACCOUNT_HOST = "account_host";
    String QUERY_PARAM_ACCOUNT_NAME = "account_name";
    String QUERY_PARAM_STATUS_ID = "status_id";
    String QUERY_PARAM_USER_KEY = "user_key";
    String QUERY_PARAM_LIST_ID = "list_id";
    String QUERY_PARAM_GROUP_ID = "group_id";
    String QUERY_PARAM_GROUP_NAME = "group_name";
    String QUERY_PARAM_SCREEN_NAME = "screen_name";
    String QUERY_PARAM_LIST_NAME = "list_name";
    String QUERY_PARAM_QUERY = "query";
    String QUERY_PARAM_TYPE = "type";
    String QUERY_PARAM_VALUE_USERS = "users";
    String QUERY_PARAM_VALUE_TWEETS = "tweets";
    String QUERY_PARAM_SHOW_NOTIFICATION = "show_notification";
    String QUERY_PARAM_NOTIFY_CHANGE = "notify_change";
    String QUERY_PARAM_LAT = "lat";
    String QUERY_PARAM_LNG = "lng";
    String QUERY_PARAM_URL = "url";
    String QUERY_PARAM_NAME = "name";
    String QUERY_PARAM_FINISH_ONLY = "finish_only";
    String QUERY_PARAM_NEW_ITEMS_COUNT = "new_items_count";
    String QUERY_PARAM_CONVERSATION_ID = "conversation_id";
    String QUERY_PARAM_READ_POSITION = "param_read_position";
    String QUERY_PARAM_LIMIT = "limit";
    String QUERY_PARAM_EXTRA = "extra";
    String QUERY_PARAM_TIMESTAMP = "timestamp";
    String QUERY_PARAM_FROM_NOTIFICATION = "from_notification";
    String QUERY_PARAM_NOTIFICATION_TYPE = "notification_type";
    String QUERY_PARAM_PREVIEW = "preview";
    String QUERY_PARAM_NOTIFY_URI = "notify_uri";

    String DEFAULT_PROTOCOL = PROTOCOL_HTTPS;

    String OAUTH_CALLBACK_OOB = "oob";
    String OAUTH_CALLBACK_URL = PROTOCOL_TWITTNUKER + "com.twitter.oauth/";

    int REQUEST_TAKE_PHOTO = 1;
    int REQUEST_PICK_MEDIA = 2;
    int REQUEST_SELECT_ACCOUNT = 3;
    int REQUEST_COMPOSE = 4;
    int REQUEST_EDIT_API = 5;
    int REQUEST_BROWSER_SIGN_IN = 6;
    int REQUEST_SET_COLOR = 7;
    int REQUEST_EDIT_IMAGE = 9;
    int REQUEST_ADD_TAB = 11;
    int REQUEST_EDIT_TAB = 12;
    int REQUEST_PICK_FILE = 13;
    int REQUEST_PICK_DIRECTORY = 14;
    int REQUEST_ADD_TO_LIST = 15;
    int REQUEST_SELECT_USER = 16;
    int REQUEST_SELECT_USER_LIST = 17;
    int REQUEST_SETTINGS = 19;
    int REQUEST_OPEN_DOCUMENT = 20;
    int REQUEST_REQUEST_PERMISSIONS = 30;
    int REQUEST_PURCHASE_EXTRA_FEATURES = 41;

    int TABLE_ID_STATUSES = 12;
    int TABLE_ID_MENTIONS = 13;
    int TABLE_ID_ACTIVITIES_ABOUT_ME = 14;
    int TABLE_ID_ACTIVITIES_BY_FRIENDS = 15;
    int TABLE_ID_MESSAGES = 21;
    int TABLE_ID_MESSAGES_CONVERSATIONS = 24;
    int TABLE_ID_FILTERED_USERS = 31;
    int TABLE_ID_FILTERED_KEYWORDS = 32;
    int TABLE_ID_FILTERED_SOURCES = 33;
    int TABLE_ID_FILTERED_LINKS = 34;
    int TABLE_ID_FILTERS_SUBSCRIPTIONS = 39;
    int TABLE_ID_TRENDS_LOCAL = 41;
    int TABLE_ID_SAVED_SEARCHES = 42;
    int TABLE_ID_SEARCH_HISTORY = 43;
    int TABLE_ID_DRAFTS = 51;
    int TABLE_ID_TABS = 52;
    int TABLE_ID_PUSH_NOTIFICATIONS = 60;
    int TABLE_ID_CACHED_USERS = 61;
    int TABLE_ID_CACHED_STATUSES = 62;
    int TABLE_ID_CACHED_HASHTAGS = 63;
    int TABLE_ID_CACHED_RELATIONSHIPS = 64;
    int VIRTUAL_TABLE_ID_CACHED_USERS_WITH_RELATIONSHIP = 121;
    int VIRTUAL_TABLE_ID_CACHED_USERS_WITH_SCORE = 122;
    int VIRTUAL_TABLE_ID_DRAFTS_UNSENT = 131;
    int VIRTUAL_TABLE_ID_DRAFTS_NOTIFICATIONS = 132;
    int VIRTUAL_TABLE_ID_SUGGESTIONS_AUTO_COMPLETE = 141;
    int VIRTUAL_TABLE_ID_SUGGESTIONS_SEARCH = 142;

    int VIRTUAL_TABLE_ID_NULL = 200;
    int VIRTUAL_TABLE_ID_EMPTY = 201;
    int VIRTUAL_TABLE_ID_DATABASE_PREPARE = 203;

    int VIRTUAL_TABLE_ID_RAW_QUERY = 300;

    int NOTIFICATION_ID_HOME_TIMELINE = 1;
    int NOTIFICATION_ID_INTERACTIONS_TIMELINE = 2;
    int NOTIFICATION_ID_DIRECT_MESSAGES = 3;
    int NOTIFICATION_ID_DRAFTS = 4;
    int NOTIFICATION_ID_USER_NOTIFICATION = 10;
    int NOTIFICATION_ID_UPDATE_STATUS = 101;
    int NOTIFICATION_ID_SEND_DIRECT_MESSAGE = 102;
    int NOTIFICATION_ID_PUSH_ERROR = 299;
    int NOTIFICATION_ID_PUSH = 301;


    int TAB_CODE_HOME_TIMELINE = 1;
    int TAB_CODE_NOTIFICATIONS_TIMELINE = 2;
    int TAB_CODE_DIRECT_MESSAGES = 4;

    String USER_TYPE_TWITTER_COM = "twitter.com";
    String USER_TYPE_FANFOU_COM = "fanfou.com";

}