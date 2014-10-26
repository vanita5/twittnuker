package de.vanita5.twittnuker.fragment.support;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.fragment.iface.IMapFragment;

public class GoogleMapFragment extends SupportMapFragment implements Constants, IMapFragment {

	private GoogleMap mMapView;

	@Override
	public void center() {
		center(true);
	}

	public void center(final boolean animate) {
		final Bundle args = getArguments();
		if (mMapView == null || args == null || !args.containsKey(EXTRA_LATITUDE) || !args.containsKey(EXTRA_LONGITUDE))
			return;
		final double lat = args.getDouble(EXTRA_LATITUDE, 0.0), lng = args.getDouble(EXTRA_LONGITUDE, 0.0);
		final CameraUpdate c = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 12);
		if (animate) {
			mMapView.animateCamera(c);
		} else {
			mMapView.moveCamera(c);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		if (args == null || !args.containsKey(EXTRA_LATITUDE) || !args.containsKey(EXTRA_LONGITUDE))
			return;
		final double lat = args.getDouble(EXTRA_LATITUDE, 0.0), lng = args.getDouble(EXTRA_LONGITUDE, 0.0);
		mMapView = getMap();
		final MarkerOptions marker = new MarkerOptions();
		marker.position(new LatLng(lat, lng));
		mMapView.addMarker(marker);
		center(false);
	}

}