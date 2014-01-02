package de.vanita5.twittnuker.view.themed;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import de.vanita5.twittnuker.util.ThemeUtils;

public class ThemedEditText extends EditText {

    public ThemedEditText(final Context context) {
        this(context, null);
    }

    public ThemedEditText(final Context context, final AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ThemedEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        setLinkTextColor(ThemeUtils.getUserLinkTextColor(context));
        setHighlightColor(ThemeUtils.getUserHighlightColor(context));
    }

}