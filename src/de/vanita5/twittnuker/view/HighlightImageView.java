package de.vanita5.twittnuker.view;

import android.content.Context;
import android.util.AttributeSet;

import de.vanita5.twittnuker.util.ThemeUtils;

public class HighlightImageView extends ForegroundImageView {

    public HighlightImageView(final Context context) {
        this(context, null);
    }

    public HighlightImageView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HighlightImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        if (isInEditMode()) return;
        setForeground(ThemeUtils.getImageHighlightDrawable(context));
    }

}