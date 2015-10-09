/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2015 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package de.vanita5.twittnuker.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.provider.TwidereCommands.Refresh;
import de.vanita5.twittnuker.util.AsyncTwitterWrapper;
import de.vanita5.twittnuker.util.dagger.DaggerGeneralComponent;

import javax.inject.Inject;

public class TwidereCommandProvider extends ContentProvider implements Constants {

    private static final UriMatcher COMMAND_URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int CODE_REFRESH_ALL = 10;
    private static final int CODE_REFRESH_HOME_TIMELINE = 11;
    private static final int CODE_REFRESH_MENTIONS = 12;
    private static final int CODE_REFRESH_INBOX = 13;
    private static final int CODE_REFRESH_OUTBOX = 14;

    static {
        COMMAND_URI_MATCHER.addURI(TwidereCommands.AUTHORITY, Refresh.ACTION_REFRESH_ALL, CODE_REFRESH_ALL);
    }

    private Context mContext;
    @Inject
    AsyncTwitterWrapper mTwitterWrapper;

    @Override
    public int delete(@NonNull final Uri uri, final String where, final String[] whereArgs) {
        return 0;
    }

    @Override
    public String getType(@NonNull final Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull final Uri uri, final ContentValues values) {
        if (handleInsertCommand(uri, values)) return uri;
        return null;
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        DaggerGeneralComponent.builder().applicationModule(TwittnukerApplication.getModule(mContext)).build().inject(this);
        return true;
    }

    @Override
    public Cursor query(@NonNull final Uri uri, final String[] projection, final String where, final String[] whereArgs,
                        final String sortOrder) {
        return handleQueryCommand(uri);
    }

    @Override
    public int update(@NonNull final Uri uri, final ContentValues values, final String where, final String[] whereArgs) {
        return 0;
    }

    private Cursor getEmptyCursor() {
        return new MatrixCursor(new String[0]);
    }

    private boolean handleInsertCommand(final Uri uri, final ContentValues values) {
        final int uri_code = COMMAND_URI_MATCHER.match(uri);
        try {
            switch (uri_code) {
                case CODE_REFRESH_ALL: {
                    mTwitterWrapper.refreshAll();
                    break;
                }
                default:
                    return false;
            }
            // something blah blah blah
        } catch (final RuntimeException e) {
            e.printStackTrace();
            if (Thread.currentThread().getId() != 1)
                throw new IllegalStateException("This method cannot be called from non-UI thread");
        }
        return true;
    }

    private Cursor handleQueryCommand(final Uri uri) {
        final int uri_code = COMMAND_URI_MATCHER.match(uri);
        try {
            switch (uri_code) {
                case CODE_REFRESH_HOME_TIMELINE:
                    if (mTwitterWrapper.isHomeTimelineRefreshing()) return getEmptyCursor();
                case CODE_REFRESH_MENTIONS:
                    if (mTwitterWrapper.isMentionsTimelineRefreshing()) return getEmptyCursor();
                case CODE_REFRESH_INBOX:
                    if (mTwitterWrapper.isReceivedDirectMessagesRefreshing())
                        return getEmptyCursor();
                case CODE_REFRESH_OUTBOX:
                    if (mTwitterWrapper.isSentDirectMessagesRefreshing()) return getEmptyCursor();
                default:
                    return null;
            }
            // something blah blah blah
        } catch (final RuntimeException e) {
            e.printStackTrace();
            if (Thread.currentThread().getId() != 1)
                throw new IllegalStateException("This method cannot be called from non-UI thread");
        }
        return null;
    }

}