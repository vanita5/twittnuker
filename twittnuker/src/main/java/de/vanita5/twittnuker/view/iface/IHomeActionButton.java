package de.vanita5.twittnuker.view.iface;

import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

public interface IHomeActionButton {
    void setButtonColor(int color);

	void setIcon(Bitmap bm);

	void setIcon(Drawable drawable);

	void setIcon(int resId);

    void setIconColor(int color, PorterDuff.Mode mode);

	void setTitle(CharSequence title);

	void setTitle(int title);
}