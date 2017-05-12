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

package de.vanita5.twittnuker.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import de.vanita5.microblog.library.twitter.model.DMResponse;
import de.vanita5.microblog.library.twitter.model.DirectMessage;
import de.vanita5.microblog.library.twitter.model.MediaEntity;
import de.vanita5.microblog.library.twitter.model.UrlEntity;
import de.vanita5.microblog.library.twitter.model.User;
import de.vanita5.twittnuker.extension.model.api.StatusExtensionsKt;
import de.vanita5.twittnuker.model.ParcelableStatus;
import de.vanita5.twittnuker.model.SpanItem;
import de.vanita5.twittnuker.model.UserKey;
import de.vanita5.twittnuker.util.database.FilterQueryBuilder;

import kotlin.Pair;

public class InternalTwitterContentUtils {

    private static final CharSequenceTranslator UNESCAPE_TWITTER_RAW_TEXT = new LookupTranslator(EntityArrays.BASIC_UNESCAPE());

    private InternalTwitterContentUtils() {
    }

    public static boolean isFiltered(final SQLiteDatabase database, final UserKey userKey,
            final String textPlain, final String quotedTextPlain,
            final SpanItem[] spans, final SpanItem[] quotedSpans,
            final String source, final String quotedSource,
            final UserKey retweetedByKey, final UserKey quotedUserKey) {
        return isFiltered(database, userKey, textPlain, quotedTextPlain, spans, quotedSpans, source,
                quotedSource, retweetedByKey, quotedUserKey, true);
    }


    public static boolean isFiltered(final SQLiteDatabase database, final UserKey userKey,
            final String textPlain, final String quotedTextPlain,
            final SpanItem[] spans, final SpanItem[] quotedSpans,
            final String source, final String quotedSource,
            final UserKey retweetedByKey, final UserKey quotedUserKey,
            final boolean filterRts) {
        if (textPlain == null && spans == null && userKey == null && source == null)
            return false;

        final Pair<String, String[]> query = FilterQueryBuilder.INSTANCE.isFilteredQuery(userKey,
                textPlain, quotedTextPlain, spans, quotedSpans, source, quotedSource, retweetedByKey,
                quotedUserKey, filterRts);
        final Cursor cur = database.rawQuery(query.getFirst(), query.getSecond());
        if (cur == null) return false;
        try {
            return cur.moveToFirst() && cur.getInt(0) != 0;
        } finally {
            cur.close();
        }
    }

    public static boolean isFiltered(@NonNull final SQLiteDatabase database,
            @NonNull final ParcelableStatus status, final boolean filterRTs) {
        return isFiltered(database, status.user_key, status.text_plain, status.quoted_text_plain,
                status.spans, status.quoted_spans, status.source, status.quoted_source,
                status.retweeted_by_user_key, status.quoted_user_key, filterRTs);
    }

    @NonNull
    public static String getBestBannerUrl(@NonNull final String baseUrl, final int width, final int height) {
        final String type;
        if (width <= 0) {
            type = "1500x500";
        } else {
            type = getBestBannerType(width, height);
        }
        final String authority = UriUtils.getAuthority(baseUrl);
        return authority != null && authority.endsWith(".twimg.com") ? baseUrl + "/" + type : baseUrl;
    }

    @NonNull
    public static String getBestBannerType(final int width, int height) {
        if (height > 0 && width / height >= 3) {
            if (width <= 300) return "300x100";
            else if (width <= 600) return "600x200";
            else return "1500x500";
        }
        if (width <= 320) return "mobile";
        else if (width <= 520) return "web";
        else if (width <= 626) return "ipad";
        else if (width <= 640) return "mobile_retina";
        else if (width <= 1040) return "web_retina";
        else return "ipad_retina";
    }

    @Nullable
    public static Pair<String, SpanItem[]> formatUserDescription(@NonNull final User user) {
        final String text = user.getDescription();
        if (text == null) return null;
        final HtmlBuilder builder = new HtmlBuilder(text, false, true, false);
        final UrlEntity[] urls = user.getDescriptionEntities();
        if (urls != null) {
            for (final UrlEntity url : urls) {
                final String expandedUrl = url.getExpandedUrl();
                if (expandedUrl != null) {
                    builder.addLink(expandedUrl, url.getDisplayUrl(), url.getStart(), url.getEnd(),
                            false);
                }
            }
        }
        return builder.buildWithIndices();
    }

    @Nullable
    public static String unescapeTwitterStatusText(final CharSequence text) {
        if (text == null) return null;
        return UNESCAPE_TWITTER_RAW_TEXT.translate(text);
    }

    @NonNull
    public static Pair<String, SpanItem[]> formatDirectMessageText(@NonNull final DirectMessage message) {
        final HtmlBuilder builder = new HtmlBuilder(message.getText(), false, true, false);
        StatusExtensionsKt.addEntities(builder, message);
        return builder.buildWithIndices();
    }

    @NonNull
    public static Pair<String, SpanItem[]> formatDirectMessageText(@NonNull final DMResponse.Entry.Message.Data message) {
        final HtmlBuilder builder = new HtmlBuilder(message.getText(), false, true, false);
        StatusExtensionsKt.addEntities(builder, message);
        return builder.buildWithIndices();
    }

    public static String getMediaUrl(MediaEntity entity) {
        return TextUtils.isEmpty(entity.getMediaUrlHttps()) ? entity.getMediaUrl() : entity.getMediaUrlHttps();
    }

    public static boolean getStartEndForEntity(UrlEntity entity, @NonNull int[] out) {
        out[0] = entity.getStart();
        out[1] = entity.getEnd();
        return true;
    }


}