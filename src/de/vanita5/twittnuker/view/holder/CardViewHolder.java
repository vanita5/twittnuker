package de.vanita5.twittnuker.view.holder;

import android.view.View;

import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.animation.CardItemAnimation;

public class CardViewHolder extends ViewHolder {

	public final CardItemAnimation item_animation;
	public final View item_menu;

	public CardViewHolder(final View view) {
		super(view);
		item_animation = new CardItemAnimation();
		item_menu = findViewById(R.id.item_menu);
	}

}
