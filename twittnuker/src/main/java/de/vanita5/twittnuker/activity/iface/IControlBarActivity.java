package de.vanita5.twittnuker.activity.iface;

public interface IControlBarActivity {

	public void setControlBarOffset(float offset);

    public void setControlBarVisibleAnimate(boolean visible);

	public float getControlBarOffset();

	public int getControlBarHeight();

    public void notifyControlBarOffsetChanged();

    public void registerControlBarOffsetListener(ControlBarOffsetListener listener);

    public void unregisterControlBarOffsetListener(ControlBarOffsetListener listener);

    public interface ControlBarOffsetListener {
        public void onControlBarOffsetChanged(IControlBarActivity activity, float offset);
    }
}