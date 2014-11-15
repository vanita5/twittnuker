package de.vanita5.twittnuker.view;

import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.menucomponent.widget.MenuBar.MenuBarListener;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.util.ThemeUtils;

public class TwidereMenuBar extends MenuBar implements MenuBarListener, Constants {
    private OnMenuItemClickListener mListener;

	public TwidereMenuBar(Context context) {
		super(context);
	}

	public TwidereMenuBar(Context context, AttributeSet attrs) {
		super(context, attrs);
        setMenuBarListener(this);
	}

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mListener = listener;
    }

	@Override
    public void onPreShowMenu(Menu menu) {
//        final int color = ThemeUtils.getThemeForegroundColor(getItemViewContext());
//        final int popupColor = ThemeUtils.getThemeForegroundColor(getPopupContext());
//        final int highlightColor = ThemeUtils.getUserAccentColor(getContext());
//        ThemeUtils.applyColorFilterToMenuIcon(menu, color, popupColor, highlightColor, Mode.SRC_ATOP,
//                MENU_GROUP_STATUS_SHARE);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return mListener != null && mListener.onMenuItemClick(item);
	}
}