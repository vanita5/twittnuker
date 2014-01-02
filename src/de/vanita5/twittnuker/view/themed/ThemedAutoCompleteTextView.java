package de.vanita5.twittnuker.view.themed;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import de.vanita5.twittnuker.util.ThemeUtils;

public class ThemedAutoCompleteTextView extends AutoCompleteTextView {

    public ThemedAutoCompleteTextView(final Context context) {
        this(context, null);
    }

    public ThemedAutoCompleteTextView(final Context context, final AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ThemedAutoCompleteTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
        setHighlightColor(ThemeUtils.getUserHighlightColor(context));
    }

}