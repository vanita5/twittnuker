package de.vanita5.twittnuker.view;

import android.content.Context;
import android.util.AttributeSet;

public class ResourceImageButton extends ResourceImageView {

	public ResourceImageButton(Context context) {
		this(context, null);
	}

	public ResourceImageButton(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.imageButtonStyle);
	}

	public ResourceImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setFocusable(true);
	}
}