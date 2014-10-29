package de.vanita5.twittnuker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.view.iface.IHomeActionButton;

public class HomeActionButtonCompat extends FrameLayout implements IHomeActionButton {

	private final ImageView mIconView;
	private final ProgressBar mProgressBar;

	public HomeActionButtonCompat(final Context context) {
		this(context, null);
	}

	public HomeActionButtonCompat(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HomeActionButtonCompat(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
        inflate(ThemeUtils.getActionBarContext(context), R.layout.action_item_home_actions_compat, this);
		mIconView = (ImageView) findViewById(android.R.id.icon);
		mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
	}

	@Override
    public void setButtonColor(int color) {
		final Drawable drawable = getBackground();
		if (drawable instanceof LayerDrawable) {
			final Drawable layer = ((LayerDrawable) drawable).findDrawableByLayerId(R.id.color_layer);
			if (layer != null) {
				layer.setColorFilter(color, Mode.SRC_ATOP);
			}
		}
    }

    @Override
    public void setIconColor(int color, Mode mode) {
        mIconView.setColorFilter(color, mode);
	}

	public void setIcon(final Bitmap bm) {
		mIconView.setImageBitmap(bm);
	}

	public void setIcon(final Drawable drawable) {
		mIconView.setImageDrawable(drawable);
	}

	public void setIcon(final int resId) {
		mIconView.setImageResource(resId);
	}

	public void setShowProgress(final boolean showProgress) {
		mProgressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
		mIconView.setVisibility(showProgress ? View.GONE : View.VISIBLE);
	}

	public void setTitle(final CharSequence title) {
		setContentDescription(title);
	}

	public void setTitle(final int title) {
		setTitle(getResources().getText(title));
	}

}