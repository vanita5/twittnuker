package de.vanita5.twittnuker.activity.support;

import android.app.ActionBar;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.menu.TwidereMenuInflater;
import de.vanita5.twittnuker.util.ParseUtils;
import de.vanita5.twittnuker.util.ThemeUtils;

public class OpenStreetMapViewerActivity extends BaseSupportActivity implements Constants {

	private MapView mMapView;
	private double mLatitude, mLongitude;

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getViewerThemeResource(this);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu, final TwidereMenuInflater inflater) {
		inflater.inflate(R.menu.menu_osm_viewer, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
			case MENU_CENTER: {
				moveToCenter(mLatitude, mLongitude);
				break;
			}
		}
		return true;
	}

	private void moveToCenter(double lat, double lng) {
		final GeoPoint gp = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		final IMapController mc = mMapView.getController();
		mc.animateTo(gp);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mMapView = (MapView) findViewById(R.id.map_view);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Uri uri = getIntent().getData();
		if (uri == null || !AUTHORITY_MAP.equals(uri.getAuthority())) {
			finish();
			return;
		}
		final double latitude = ParseUtils.parseDouble(uri.getQueryParameter(QUERY_PARAM_LAT), Double.NaN);
		final double longitude = ParseUtils.parseDouble(uri.getQueryParameter(QUERY_PARAM_LNG), Double.NaN);
		if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
			finish();
			return;
		}
		mLatitude = latitude;
		mLongitude = longitude;
		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		setContentView(R.layout.activity_osm_viewer);
		mMapView.setMultiTouchControls(true);
		mMapView.setBuiltInZoomControls(true);
		final List<Overlay> overlays = mMapView.getOverlays();
		final GeoPoint gp = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
		final Drawable d = getResources().getDrawable(R.drawable.ic_map_marker);
		final Itemization markers = new Itemization(d, mMapView.getResourceProxy());
		final OverlayItem overlayitem = new OverlayItem("", "", gp);
		markers.addOverlay(overlayitem);
		overlays.add(markers);
		final IMapController mc = mMapView.getController();
		mc.setZoom(12);
		mc.setCenter(gp);
	}


	static class Itemization extends ItemizedOverlay<OverlayItem> {

		private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public Itemization(final Drawable defaultMarker, final ResourceProxy proxy) {
			super(boundCenterBottom(defaultMarker), proxy);
		}

		public void addOverlay(final OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		public boolean onSnapToItem(final int x, final int y, final Point snapPoint, final IMapView mapView) {
			return false;
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		@Override
		protected OverlayItem createItem(final int i) {
			return mOverlays.get(i);
		}

		protected static Drawable boundCenterBottom(final Drawable d) {
			d.setBounds(-d.getIntrinsicWidth() / 2, -d.getIntrinsicHeight(), d.getIntrinsicWidth() / 2, 0);
			return d;
		}

	}
}