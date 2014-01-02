package de.vanita5.twittnuker.view.themed;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

import de.vanita5.twittnuker.util.ThemeUtils;

public class ThemedMultiAutoCompleteTextView extends MultiAutoCompleteTextView {

    public ThemedMultiAutoCompleteTextView(final Context context) {
        this(context, null);
    }

    public ThemedMultiAutoCompleteTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ThemedMultiAutoCompleteTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
        setHighlightColor(ThemeUtils.getUserHighlightColor(context));
    }

}