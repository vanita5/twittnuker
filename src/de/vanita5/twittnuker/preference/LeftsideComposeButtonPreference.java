package de.vanita5.twittnuker.preference;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.util.AttributeSet;

import de.vanita5.twittnuker.util.SmartBarUtils;

public class LeftsideComposeButtonPreference extends CheckBoxPreference {

    public LeftsideComposeButtonPreference(final Context context) {
        super(context);
    }

    public LeftsideComposeButtonPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public LeftsideComposeButtonPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDependencyChanged(final Preference dependency, final boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent || SmartBarUtils.hasSmartBar());
    }

}