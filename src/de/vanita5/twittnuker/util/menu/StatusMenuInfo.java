package de.vanita5.twittnuker.util.menu;

import android.view.ContextMenu.ContextMenuInfo;

public class StatusMenuInfo implements ContextMenuInfo {
	private final boolean highlight;

	public StatusMenuInfo(boolean highlight) {
		this.highlight = highlight;
	}

	public boolean isHighlight() {
		return highlight;
	}
}