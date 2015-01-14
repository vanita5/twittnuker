/*
 * Twittnuker - Twitter client for Android
 *
 * Copyright (C) 2013-2014 vanita5 <mail@vanita5.de>
 *
 * This program incorporates a modified version of Twidere.
 * Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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
import android.content.Intent;
import android.os.Bundle;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.activity.support.ComposeActivity;
import de.vanita5.twittnuker.util.SharedPreferencesWrapper;

public class AssistLauncherActivity extends Activity implements Constants {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final SharedPreferencesWrapper prefs = SharedPreferencesWrapper.getInstance(this, SHARED_PREFERENCES_NAME,
				MODE_PRIVATE);
		final String composeNowAction = prefs.getString(KEY_COMPOSE_NOW_ACTION, VALUE_COMPOSE_NOW_ACTION_COMPOSE), action;
		if (VALUE_COMPOSE_NOW_ACTION_TAKE_PHOTO.equals(composeNowAction)) {
			action = INTENT_ACTION_COMPOSE_TAKE_PHOTO;
		} else if (VALUE_COMPOSE_NOW_ACTION_PICK_IMAGE.equals(composeNowAction)) {
			action = INTENT_ACTION_COMPOSE_PICK_IMAGE;
		} else {
			action = INTENT_ACTION_COMPOSE;
		}
		final Intent intent = new Intent(action);
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		intent.setClass(this, ComposeActivity.class);
		startActivity(intent);
		finish();
	}

}