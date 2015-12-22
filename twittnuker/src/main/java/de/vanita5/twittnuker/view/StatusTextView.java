package de.vanita5.twittnuker.view;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;

import de.vanita5.twittnuker.text.util.EmojiSpannableFactory;
import de.vanita5.twittnuker.text.util.SafeEditableFactory;
import de.vanita5.twittnuker.view.themed.ThemedTextView;

public class StatusTextView extends ThemedTextView {

    public StatusTextView(final Context context) {
        super(context);
        init();
    }

    public StatusTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        // Android clears TextView when setText(), so setText before touch
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            final CharSequence text = getText();
            setText(null);
            setText(text);
        }
        return super.dispatchTouchEvent(event);
    }

    private void init() {
        setEditableFactory(new SafeEditableFactory());
        setSpannableFactory(new EmojiSpannableFactory(this));
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return LinkMovementMethod.getInstance();
    }

}
