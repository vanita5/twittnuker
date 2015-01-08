package de.vanita5.twittnuker.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class DrawerAccountHeaderContainer extends RelativeLayout {
	public DrawerAccountHeaderContainer(Context context) {
		super(context);
	}

	public DrawerAccountHeaderContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DrawerAccountHeaderContainer(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec), height = width / 2;
		setMeasuredDimension(width, height);
		super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}
}