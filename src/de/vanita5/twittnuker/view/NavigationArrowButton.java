package de.vanita5.twittnuker.view;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageButton;
import de.vanita5.twittnuker.util.ArrayUtils;
import de.vanita5.twittnuker.util.ThemeUtils;


public class NavigationArrowButton extends ImageButton {

	private final int mHighlightColor;

	public NavigationArrowButton(final Context context) {
		this(context, null);
	}

	public NavigationArrowButton(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.imageButtonStyle);
	}

	public NavigationArrowButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mHighlightColor = isInEditMode() ? 0 : ThemeUtils.getUserThemeColor(context);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		updateColorFilter();
	}

	private void updateColorFilter() {
		if (isClickable() && isEnabled() && ArrayUtils.contains(getDrawableState(), android.R.attr.state_pressed)) {
			setColorFilter(mHighlightColor, Mode.MULTIPLY);
		} else {
			clearColorFilter();
		}
	}
}
