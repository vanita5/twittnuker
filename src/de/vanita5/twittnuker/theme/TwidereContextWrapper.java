package de.vanita5.twittnuker.theme;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;

public class TwidereContextWrapper extends ContextWrapper {

    private final Resources mResources;

    public TwidereContextWrapper(final Context base, final Resources res) {
        super(base);
        mResources = res;
    }

    @Override
    public Resources getResources() {
        return mResources;
    }

}