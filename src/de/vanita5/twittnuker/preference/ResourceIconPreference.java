package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

import de.vanita5.twittnuker.R;

public class ResourceIconPreference extends Preference {
	public ResourceIconPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ResourceIconPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ResourceIconPreference(Context context) {
		this(context, null);
	}
}