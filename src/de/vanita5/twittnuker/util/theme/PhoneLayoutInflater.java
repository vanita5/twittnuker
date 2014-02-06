package de.vanita5.twittnuker.util.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class PhoneLayoutInflater extends LayoutInflater {
    private static final String[] sClassPrefixList = { "android.widget.", "android.webkit." };

    /**
     * Instead of instantiating directly, you should retrieve an instance
     * through {@link Context#getSystemService}
     *
     * @param context The Context in which in which to find resources and other
     *            application-specific things.
     *
     * @see Context#getSystemService
     */
    public PhoneLayoutInflater(final Context context) {
        super(context);
    }

    protected PhoneLayoutInflater(final LayoutInflater original, final Context newContext) {
        super(original, newContext);
    }

    @Override
    public LayoutInflater cloneInContext(final Context newContext) {
        return new PhoneLayoutInflater(this, newContext);
    }

    /**
     * Override onCreateView to instantiate names that correspond to the widgets
     * known to the Widget factory. If we don't find a match, call through to
     * our super class.
     */
    @Override
    protected View onCreateView(final String name, final AttributeSet attrs) throws ClassNotFoundException {
        for (final String prefix : sClassPrefixList) {
            try {
                final View view = createView(name, prefix, attrs);
                if (view != null) return view;
            } catch (final ClassNotFoundException e) {
                // In this case we want to let the base class take a crack
                // at it.
            }
        }

        return super.onCreateView(name, attrs);
    }
}