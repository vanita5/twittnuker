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

package de.vanita5.twittnuker.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.TextView;

import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.Expression;
import org.mariotaku.querybuilder.OrderBy;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.app.TwittnukerApplication;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedHashtags;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedUsers;
import de.vanita5.twittnuker.provider.TwidereDataStore.CachedValues;
import de.vanita5.twittnuker.util.MediaLoaderWrapper;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;
import de.vanita5.twittnuker.util.Utils;
import de.vanita5.twittnuker.view.ShapedImageView;

public class UserHashtagAutoCompleteAdapter extends SimpleCursorAdapter implements Constants {

	private static final String[] FROM = new String[0];
	private static final int[] TO = new int[0];

    @NonNull
	private final ContentResolver mResolver;
    @NonNull
	private final SQLiteDatabase mDatabase;
    @NonNull
	private final MediaLoaderWrapper mProfileImageLoader;
	@NonNull
	private final SharedPreferencesWrapper mPreferences;

	private final EditText mEditText;

	private final boolean mDisplayProfileImage;
    private final int mProfileImageStyle;

	private int mProfileImageUrlIdx, mNameIdx, mScreenNameIdx, mUserIdIdx;
	private char mToken = '@';
    private long mAccountId;

	public UserHashtagAutoCompleteAdapter(final Context context) {
		this(context, null);
	}

	public UserHashtagAutoCompleteAdapter(final Context context, final EditText view) {
		super(context, R.layout.list_item_two_line_small, null, FROM, TO, 0);
		mEditText = view;
        mPreferences = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = context.getContentResolver();
		final TwittnukerApplication app = TwittnukerApplication.getInstance(context);
        mProfileImageLoader = app.getMediaLoaderWrapper();
        mDatabase = app.getSQLiteDatabase();
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
	}

	public UserHashtagAutoCompleteAdapter(final EditText view) {
		this(view.getContext(), view);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		if (isCursorClosed()) return;
		final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
		final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        final ShapedImageView icon = (ShapedImageView) view.findViewById(android.R.id.icon);

		// Clear images in prder to prevent images in recycled view shown.
		icon.setImageDrawable(null);

		if (mScreenNameIdx != -1 && mNameIdx != -1 && mUserIdIdx != -1) {
			text1.setText(cursor.getString(mNameIdx));
			text2.setText("@" + cursor.getString(mScreenNameIdx));
		} else {
			text1.setText("#" + cursor.getString(mNameIdx));
			text2.setText(R.string.hashtag);
		}
		icon.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mProfileImageUrlIdx != -1) {
            if (mDisplayProfileImage) {
                final String profileImageUrl = cursor.getString(mProfileImageUrlIdx);
                mProfileImageLoader.displayProfileImage(icon, profileImageUrl);
                icon.setStyle(mProfileImageStyle);
			} else {
                mProfileImageLoader.cancelDisplayTask(icon);
			}
            icon.clearColorFilter();
		} else {
            icon.setStyle(mProfileImageStyle);
            icon.setImageResource(R.drawable.ic_action_hashtag);
            icon.setColorFilter(text1.getCurrentTextColor(), Mode.SRC_ATOP);
		}
		super.bindView(view, context, cursor);
	}

	public void closeCursor() {
		final Cursor cursor = getCursor();
		if (cursor == null) return;
		if (!cursor.isClosed()) {
			cursor.close();
		}
	}

	@Override
	public CharSequence convertToString(final Cursor cursor) {
		if (isCursorClosed()) return null;
		return cursor.getString(mScreenNameIdx != -1 ? mScreenNameIdx : mNameIdx);
	}

	public boolean isCursorClosed() {
		final Cursor cursor = getCursor();
		return cursor == null || cursor.isClosed();
	}

	@Override
	public Cursor runQueryOnBackgroundThread(final CharSequence constraint) {
		char token = mToken;
		if (mEditText != null && constraint != null) {
			final CharSequence text = mEditText.getText();
			token = text.charAt(mEditText.getSelectionEnd() - constraint.length() - 1);
		}
		if (isAtSymbol(token) == isAtSymbol(mToken)) {
			final FilterQueryProvider filter = getFilterQueryProvider();
			if (filter != null) return filter.runQuery(constraint);
		}
		mToken = token;
        final String constraintEscaped = constraint != null ? constraint.toString().replaceAll("_", "^_") : null;
		if (isAtSymbol(token)) {
            final Expression selection;
            final String[] selectionArgs;
            if (constraintEscaped != null) {
                selection = Expression.or(Expression.likeRaw(new Column(CachedUsers.SCREEN_NAME), "?||'%'", "^"),
                        Expression.likeRaw(new Column(CachedUsers.NAME), "?||'%'", "^"));
                selectionArgs = new String[]{constraintEscaped, constraintEscaped};
            } else {
                selection = null;
                selectionArgs = null;
            }
            final OrderBy orderBy = new OrderBy(new String[]{CachedUsers.LAST_SEEN, "score", CachedUsers.SCREEN_NAME,
                    CachedUsers.NAME}, new boolean[]{false, false, true, true});
            final Cursor cursor = mResolver.query(Uri.withAppendedPath(CachedUsers.CONTENT_URI_WITH_SCORE, String.valueOf(mAccountId)),
                    CachedUsers.BASIC_COLUMNS, selection != null ? selection.getSQL() : null, selectionArgs, orderBy.getSQL());
            if (Utils.isDebugBuild() && cursor == null) throw new NullPointerException();
            return cursor;
        } else {
            final String selection = constraintEscaped != null ? CachedHashtags.NAME + " LIKE ?||'%' ESCAPE '^'"
					: null;
            final String[] selectionArgs = constraintEscaped != null ? new String[]{constraintEscaped} : null;
            final Cursor cursor = mDatabase.query(true, CachedHashtags.TABLE_NAME, CachedHashtags.COLUMNS, selection, selectionArgs,
					null, null, CachedHashtags.NAME, null);
            if (Utils.isDebugBuild() && cursor == null) throw new NullPointerException();
            return cursor;
		}
	}


    public void setAccountId(long accountId) {
        mAccountId = accountId;
    }

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		if (cursor != null) {
			mNameIdx = cursor.getColumnIndex(CachedValues.NAME);
			mScreenNameIdx = cursor.getColumnIndex(CachedUsers.SCREEN_NAME);
			mUserIdIdx = cursor.getColumnIndex(CachedUsers.USER_ID);
			mProfileImageUrlIdx = cursor.getColumnIndex(CachedUsers.PROFILE_IMAGE_URL);
		}
		return super.swapCursor(cursor);
	}


	private static boolean isAtSymbol(final char character) {
		switch (character) {
			case '\uff20':
			case '@':
				return true;
		}
		return false;
	}

}