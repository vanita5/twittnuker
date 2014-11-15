package de.vanita5.twittnuker.util.menu;

import android.view.ContextMenu.ContextMenuInfo;

public class TwidereMenuInfo implements ContextMenuInfo {
	private final boolean highlight;

	public TwidereMenuInfo(boolean highlight) {
		this.highlight = highlight;
	}

	public boolean isHighlight() {
		return highlight;
	}
}