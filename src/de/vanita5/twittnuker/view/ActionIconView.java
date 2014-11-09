package de.vanita5.twittnuker.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ActionIconView extends ImageView {

	public ActionIconView(Context context) {
		this(context, null);
	}

	public ActionIconView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ActionIconView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		final TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.colorForeground});
		setColorFilter(a.getColor(0, 0), Mode.SRC_ATOP);
		a.recycle();
	}
}