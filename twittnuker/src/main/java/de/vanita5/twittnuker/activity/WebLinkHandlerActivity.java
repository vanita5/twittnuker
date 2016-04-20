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

package de.vanita5.twittnuker.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.util.BugReporter;
import de.vanita5.twittnuker.util.IntentUtils;
import de.vanita5.twittnuker.util.Utils;

import java.util.List;

public class WebLinkHandlerActivity extends Activity implements Constants {

    @SuppressWarnings("SpellCheckingInspection")
    public static final String[] TWITTER_RESERVED_PATHS = {"about", "account", "accounts", "activity", "all",
            "announcements", "anywhere", "api_rules", "api_terms", "apirules", "apps", "auth", "badges", "blog",
            "business", "buttons", "contacts", "devices", "direct_messages", "download", "downloads",
            "edit_announcements", "faq", "favorites", "find_sources", "find_users", "followers", "following",
            "friend_request", "friendrequest", "friends", "goodies", "help", "home", "im_account", "inbox",
            "invitations", "invite", "jobs", "list", "login", "logo", "logout", "me", "mentions", "messages",
            "mockview", "newtwitter", "notifications", "nudge", "oauth", "phoenix_search", "positions", "privacy",
            "public_timeline", "related_tweets", "replies", "retweeted_of_mine", "retweets", "retweets_by_others",
            "rules", "saved_searches", "search", "sent", "settings", "share", "signup", "signin", "similar_to",
            "statistics", "terms", "tos", "translate", "trends", "tweetbutton", "twttr", "update_discoverability",
            "users", "welcome", "who_to_follow", "widgets", "zendesk_auth", "media_signup"};

    @SuppressWarnings("SpellCheckingInspection")
    public static final String[] FANFOU_RESERVED_PATHS = {"home", "privatemsg", "finder", "browse",
            "search", "settings", "message", "mentions", "favorites", "friends", "followers",
            "sharer", "photo", "album", "paipai", "q", "userview", "dialogue"};


    private static final String AUTHORITY_TWITTER_COM = "twitter.com";


    private static Uri regulateTwitterUri(Uri data) {
        final String encodedFragment = data.getEncodedFragment();
        if (encodedFragment != null && encodedFragment.startsWith("!/")) {
            return regulateTwitterUri(Uri.parse("https://twitter.com" + encodedFragment.substring(1)));
        }
        final Uri.Builder builder = data.buildUpon();
        builder.scheme("https");
        builder.authority(AUTHORITY_TWITTER_COM);
        return builder.build();
    }


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PackageManager packageManager = getPackageManager();
        final Intent intent = getIntent();
        intent.setExtrasClassLoader(TwittnukerApplication.class.getClassLoader());
        final Uri uri = intent.getData();
        if (uri == null || uri.getHost() == null) {
            finish();
            return;
        }
        final Pair<Intent, Boolean> handled;
        switch (uri.getHost()) {
            case "twitter.com":
            case "www.twitter.com":
            case "mobile.twitter.com": {
                handled = handleTwitterLink(regulateTwitterUri(uri));
                break;
            }
            case "fanfou.com": {
                handled = handleFanfouLink(uri);
                break;
            }
            default: {
                handled = Pair.create(null, false);
                break;
            }
        }
        if (handled.first != null) {
            handled.first.putExtras(intent);
            startActivity(handled.first);
        } else {
            if (!handled.second) {
                BugReporter.logException(new TwitterLinkException("Unable to handle twitter uri " + uri));
            }
            final Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, uri);
            fallbackIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            fallbackIntent.setPackage(IntentUtils.getDefaultBrowserPackage(this, uri, false));
            final ComponentName componentName = fallbackIntent.resolveActivity(packageManager);
            if (componentName == null) {
                final Intent targetIntent = new Intent(Intent.ACTION_VIEW, uri);
                targetIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                startActivity(Intent.createChooser(targetIntent, getString(R.string.open_in_browser)));
            } else if (!TextUtils.equals(getPackageName(), componentName.getPackageName())) {
                startActivity(fallbackIntent);
            } else {
                // TODO show error
            }
        }
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setVisible(true);
    }

    @NonNull
    private Pair<Intent, Boolean> handleFanfouLink(final Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() > 0) {
            switch (pathSegments.get(0)) {
                case "statuses": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_STATUS);
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_FANFOU_COM);
                    builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, pathSegments.get(1));
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                default: {
                    if (!ArrayUtils.contains(FANFOU_RESERVED_PATHS, pathSegments.get(0))) {
                        if (pathSegments.size() == 1) {
                            final Uri.Builder builder = new Uri.Builder();
                            builder.scheme(SCHEME_TWITTNUKER);
                            builder.authority(AUTHORITY_USER);
                            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_FANFOU_COM);
                            final UserKey userKey = new UserKey(pathSegments.get(0), USER_TYPE_FANFOU_COM);
                            builder.appendQueryParameter(QUERY_PARAM_USER_KEY, userKey.toString());
                            return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                        }
                    }
                    return Pair.create(null, false);
                }
            }
        }
        return Pair.create(null, false);
    }

    @NonNull
    private Pair<Intent, Boolean> handleTwitterLink(final Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() > 0) {
            switch (pathSegments.get(0)) {
                case "i": {
                    return getIUriIntent(uri, pathSegments);
                }
                case "intent": {
                    return getTwitterIntentUriIntent(uri, pathSegments);
                }
                case "share": {
                    final Intent handledIntent = new Intent(this, ComposeActivity.class);
                    handledIntent.setAction(Intent.ACTION_SEND);
                    final String text = uri.getQueryParameter("text");
                    final String url = uri.getQueryParameter("url");
                    handledIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareStatus(this, text, url));
                    return Pair.create(handledIntent, true);
                }
                case "search": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_SEARCH);
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
                    builder.appendQueryParameter(QUERY_PARAM_QUERY, uri.getQueryParameter("q"));
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "following": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_USER_FRIENDS);
                    builder.appendQueryParameter(QUERY_PARAM_USER_KEY, UserKey.SELF_REFERENCE.toString());
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "followers": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_USER_FOLLOWERS);
                    builder.appendQueryParameter(QUERY_PARAM_USER_KEY, UserKey.SELF_REFERENCE.toString());
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "favorites": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_USER_FAVORITES);
                    builder.appendQueryParameter(QUERY_PARAM_USER_KEY, UserKey.SELF_REFERENCE.toString());
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                default: {
                    if (ArrayUtils.contains(TWITTER_RESERVED_PATHS, pathSegments.get(0))) {
                        return Pair.create(null, true);
                    }
                    return handleUserSpecificPageIntent(uri, pathSegments, pathSegments.get(0));
                }
            }
        }
        return Pair.create(null, false);
    }

    @NonNull
    private Pair<Intent, Boolean> handleUserSpecificPageIntent(Uri uri, List<String> pathSegments, String screenName) {
        final int segsSize = pathSegments.size();
        if (segsSize == 1) {
            final Uri.Builder builder = new Uri.Builder();
            builder.scheme(SCHEME_TWITTNUKER);
            builder.authority(AUTHORITY_USER);
            builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
            builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
            return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
        } else if (segsSize == 2) {
            switch (pathSegments.get(1)) {
                case "following": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_USER_FRIENDS);
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "followers": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_USER_FOLLOWERS);
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                case "favorites": {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_USER_FAVORITES);
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
                default: {
                    final Uri.Builder builder = new Uri.Builder();
                    builder.scheme(SCHEME_TWITTNUKER);
                    builder.authority(AUTHORITY_USER_LIST);
                    builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
                    builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                    builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments.get(1));
                    return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                }
            }
        } else if (segsSize >= 3) {
            final long def = -1;
            if ("status".equals(pathSegments.get(1)) && NumberUtils.toLong(pathSegments.get(2), def) != -1) {
                final Uri.Builder builder = new Uri.Builder();
                builder.scheme(SCHEME_TWITTNUKER);
                builder.authority(AUTHORITY_STATUS);
                builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
                builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, pathSegments.get(2));
                return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
            } else {
                switch (pathSegments.get(2)) {
                    case "members": {
                        final Uri.Builder builder = new Uri.Builder();
                        builder.scheme(SCHEME_TWITTNUKER);
                        builder.authority(AUTHORITY_USER_LIST_MEMBERS);
                        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
                        builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                        builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments.get(1));
                        return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                    }
                    case "subscribers": {
                        final Uri.Builder builder = new Uri.Builder();
                        builder.scheme(SCHEME_TWITTNUKER);
                        builder.authority(AUTHORITY_USER_LIST_SUBSCRIBERS);
                        builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_HOST, USER_TYPE_TWITTER_COM);
                        builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, screenName);
                        builder.appendQueryParameter(QUERY_PARAM_LIST_NAME, pathSegments.get(1));
                        return Pair.create(new Intent(Intent.ACTION_VIEW, builder.build()), true);
                    }
                }
            }
        }
        return Pair.create(null, false);
    }

    private Pair<Intent, Boolean> getTwitterIntentUriIntent(Uri uri, List<String> pathSegments) {
        if (pathSegments.size() < 2) return Pair.create(null, false);
        switch (pathSegments.get(1)) {
            case "tweet": {
                final Intent handledIntent = new Intent(this, ComposeActivity.class);
                handledIntent.setAction(Intent.ACTION_SEND);
                final String text = uri.getQueryParameter("text");
                final String url = uri.getQueryParameter("url");
                handledIntent.putExtra(Intent.EXTRA_TEXT, Utils.getShareStatus(this, text, url));
                return Pair.create(handledIntent, true);
            }
        }
        return Pair.create(null, false);
    }

    private Pair<Intent, Boolean> getIUriIntent(Uri uri, List<String> pathSegments) {
        return Pair.create(null, false);
    }

    private class TwitterLinkException extends Exception {
        public TwitterLinkException(final String s) {
            super(s);
        }
    }
}