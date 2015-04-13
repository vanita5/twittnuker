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

package de.vanita5.twittnuker.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;
import android.view.KeyEvent;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.activity.support.ComposeActivity;
import de.vanita5.twittnuker.activity.support.QuickSearchBarActivity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class KeyboardShortcutsHandler implements Constants {

	private static final HashMap<String, Integer> sActionLabelMap = new HashMap<>();
    private static final SparseArrayCompat<String> sMetaNameMap = new SparseArrayCompat<>();

	static {
		sActionLabelMap.put("compose", R.string.compose);
		sActionLabelMap.put("search", android.R.string.search_go);

		sMetaNameMap.put(KeyEvent.META_FUNCTION_ON, "fn");
		sMetaNameMap.put(KeyEvent.META_META_ON, "meta");
		sMetaNameMap.put(KeyEvent.META_CTRL_ON, "ctrl");
		sMetaNameMap.put(KeyEvent.META_ALT_ON, "alt");
		sMetaNameMap.put(KeyEvent.META_SHIFT_ON, "shift");
	}

	private static final String KEYCODE_STRING_PREFIX = "KEYCODE_";
	private final Context mContext;
	private final SharedPreferencesWrapper mPreferences;

    public static int getKeyEventMeta(String name) {
        for (int i = 0, j = sMetaNameMap.size(); i < j; i++) {
            if (sMetaNameMap.valueAt(i).equalsIgnoreCase(name)) return sMetaNameMap.keyAt(i);
        }
        return 0;
    }

	public KeyboardShortcutsHandler(final Context context) {
		mContext = context;
		mPreferences = SharedPreferencesWrapper.getInstance(context, KEYBOARD_SHORTCUTS_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	public static String getActionLabel(Context context, String action) {
		if (!sActionLabelMap.containsKey(action)) return null;
		final int labelRes = sActionLabelMap.get(action);
		return context.getString(labelRes);
	}

    public static String metaToHumanReadableString(int metaState) {
        final StringBuilder keyNameBuilder = new StringBuilder();
        for (int i = 0, j = sMetaNameMap.size(); i < j; i++) {
            if ((sMetaNameMap.keyAt(i) & metaState) != 0) {
                final String value = sMetaNameMap.valueAt(i);
                keyNameBuilder.append(value.substring(0, 1).toUpperCase(Locale.US));
                keyNameBuilder.append(value.substring(1));
                keyNameBuilder.append("+");
            }
        }
        return keyNameBuilder.toString();
    }

	public static Set<String> getActions() {
		return sActionLabelMap.keySet();
	}

	public static String getKeyEventKey(String contextTag, int keyCode, KeyEvent event) {
		final StringBuilder keyNameBuilder = new StringBuilder();
		if (!TextUtils.isEmpty(contextTag)) {
			keyNameBuilder.append(contextTag);
            keyNameBuilder.append(".");
		}
        final int metaState = KeyEvent.normalizeMetaState(event.getMetaState());

        for (int i = 0, j = sMetaNameMap.size(); i < j; i++) {
            if ((sMetaNameMap.keyAt(i) & metaState) != 0) {
                keyNameBuilder.append(sMetaNameMap.valueAt(i));
                keyNameBuilder.append("+");
			}
		}
		final String keyCodeString = KeyEvent.keyCodeToString(keyCode);
		if (keyCodeString.startsWith(KEYCODE_STRING_PREFIX)) {
			keyNameBuilder.append(keyCodeString.substring(KEYCODE_STRING_PREFIX.length()).toLowerCase(Locale.US));
		}
		return keyNameBuilder.toString();
	}

	public boolean handleKey(final String contextTag, final int keyCode, final KeyEvent event) {
		if (!isValidForHotkey(keyCode, event)) return false;
		final String key = getKeyEventKey(contextTag, keyCode, event);
		final String action = mPreferences.getString(key, null);
		if (action == null) return false;
		switch (action) {
			case "compose": {
				mContext.startActivity(new Intent(mContext, ComposeActivity.class).setAction(INTENT_ACTION_COMPOSE));
				return true;
			}
			case "search": {
				mContext.startActivity(new Intent(mContext, QuickSearchBarActivity.class).setAction(INTENT_ACTION_QUICK_SEARCH));
				return true;
			}
		}
		return false;
	}

	public static boolean isValidForHotkey(int keyCode, KeyEvent event) {
		return !event.isSystem() && !KeyEvent.isModifierKey(keyCode) && keyCode != KeyEvent.KEYCODE_UNKNOWN;
	}

}