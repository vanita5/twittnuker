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

package de.vanita5.twittnuker.provider;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ShareProvider extends ContentProvider {
    public static final String[] COLUMNS = {MediaColumns.DATA, MediaColumns.DISPLAY_NAME,
            MediaColumns.SIZE, MediaColumns.MIME_TYPE};

    @Override
    public boolean onCreate() {
        return true;
    }

    @SuppressLint("SetWorldReadable")
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            final File file = getFile(uri);
            if (file == null) return null;
            // Make world-readable intentionally since it will be deleted shortly
            //noinspection ResultOfMethodCallIgnored
            file.setReadable(true, false);
            if (projection == null) {
                projection = COLUMNS;
            }
            MatrixCursor cursor = new MatrixCursor(projection, 1);
            Object[] values = new Object[projection.length];
            writeValue(projection, values, MediaColumns.DATA, file.getAbsolutePath());
            cursor.addRow(values);
            return cursor;
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        if (!mode.equals("r")) throw new IllegalArgumentException();
        final File file = getFile(uri);
        return ParcelFileDescriptor.open(file,
                ParcelFileDescriptor.MODE_READ_ONLY);
    }

    private void writeValue(String[] columns, Object[] values, String column, Object value) {
        int idx = ArrayUtils.indexOf(columns, column);
        if (idx != ArrayUtils.INDEX_NOT_FOUND) {
            values[idx] = value;
        }
    }

    private File getFile(@NonNull Uri uri) throws FileNotFoundException {
        final String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment == null) throw new FileNotFoundException(uri.toString());
        return new File(getFilesDir(getContext()), lastPathSegment);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Nullable
    public static File getFilesDir(Context context) {
        final File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) return null;
        return new File(externalCacheDir, "shared_files");
    }

    @Nullable
    public static Uri getUriForFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        final File filesDir = getFilesDir(context);
        if (filesDir == null) return null;
        if (!filesDir.equals(file.getParentFile())) return null;
        return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(authority).appendPath(file.getName()).build();
    }

    public static boolean clearTempFiles(Context context) {
        final File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir == null) return false;
        File[] files = externalCacheDir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
        }
        return true;
    }
}