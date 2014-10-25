package de.vanita5.twittnuker.view.iface;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public interface IHomeActionButton {
	void setColor(int color);

	void setIcon(Bitmap bm);

	void setIcon(Drawable drawable);

	void setIcon(int resId);

	void setShowProgress(boolean showProgress);

	void setTitle(CharSequence title);

	void setTitle(int title);
}