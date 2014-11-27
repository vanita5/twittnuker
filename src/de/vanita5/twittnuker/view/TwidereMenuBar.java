package de.vanita5.twittnuker.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.ImageView;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.menucomponent.widget.MenuBar.MenuBarListener;
import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.util.ThemeUtils;
import de.vanita5.twittnuker.util.Utils;

public class TwidereMenuBar extends MenuBar implements MenuBarListener, Constants {
    private final int mItemColor, mPopupItemColor, mHighlightColor;
    private OnMenuItemClickListener mListener;

	public TwidereMenuBar(Context context) {
        this(context, null);
	}

	public TwidereMenuBar(Context context, AttributeSet attrs) {
		super(context, attrs);
        final int itemBackgroundColor = ThemeUtils.getThemeBackgroundColor(getItemViewContext());
        final int popupItemBackgroundColor = ThemeUtils.getThemeBackgroundColor(getPopupContext());
        final Resources resources = getResources();
        final int colorDark = resources.getColor(R.color.action_icon_dark);
        final int colorLight = resources.getColor(R.color.action_icon_light);
        mItemColor = Utils.getContrastYIQ(itemBackgroundColor, colorDark, colorLight);
        mPopupItemColor = Utils.getContrastYIQ(popupItemBackgroundColor, colorDark, colorLight);
        mHighlightColor = ThemeUtils.getUserAccentColor(getContext());
        setMenuBarListener(this);
	}

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mListener = listener;
    }

	@Override
    public void onPreShowMenu(Menu menu) {
        ThemeUtils.applyColorFilterToMenuIcon(menu, mItemColor, mPopupItemColor, mHighlightColor,
                Mode.SRC_ATOP, MENU_GROUP_STATUS_SHARE);
    }

    @Override
    public void onPostShowMenu(Menu menu) {
        final View overflowItemView = getOverflowItemView();
        if (overflowItemView instanceof ImageView) {
            ((ImageView) overflowItemView).setColorFilter(mItemColor, Mode.SRC_ATOP);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mListener != null && mListener.onMenuItemClick(item);
	}
}