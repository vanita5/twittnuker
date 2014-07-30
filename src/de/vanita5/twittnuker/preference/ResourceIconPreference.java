package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;

import de.vanita5.twittnuker.R;

public class ResourceIconPreference extends Preference {
	public ResourceIconPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.ResourceImageView, defStyle, 0);
		final int resId = array.getResourceId(R.styleable.ResourceImageView_image, -1);
		if (resId > 0) {
			setIcon(context.getResources().getDrawable(resId));
		}
		array.recycle();
	}

	public ResourceIconPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ResourceIconPreference(Context context) {
		this(context, null);
	}
}