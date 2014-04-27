package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.util.AttributeSet;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;

public class PushNotificationContentPreference extends MultiSelectListPreference implements Constants {

	protected PushNotificationContentPreference(Context context) {
		super(context);
	}

	public PushNotificationContentPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public PushNotificationContentPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected boolean[] getDefaults() {
		return new boolean[] { DEFAULT_MENTIONS_NOTIFICATION, DEFAULT_DIRECT_MESSAGES_NOTIFICATION,
					DEFAULT_NEW_FOLLOWERS_NOTIFICATION };
	}

	@Override
	protected String[] getKeys() {
		return new String[] { KEY_MENTIONS_NOTIFICATION_PUSH, KEY_DIRECT_MESSAGES_NOTIFICATION_PUSH,
					KEY_NEW_FOLLOWERS_NOTIFICATION_PUSH };
	}

	@Override
	protected String[] getNames() {
		return getContext().getResources().getStringArray(R.array.entries_push_notification_content);
	}
}
