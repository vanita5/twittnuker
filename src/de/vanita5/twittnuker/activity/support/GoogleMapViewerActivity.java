package de.vanita5.twittnuker.activity.support;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import de.vanita5.twittnuker.Constants;
import de.vanita5.twittnuker.R;
import de.vanita5.twittnuker.fragment.iface.IMapFragment;
import de.vanita5.twittnuker.fragment.support.GoogleMapFragment;
import de.vanita5.twittnuker.fragment.support.WebMapFragment;
import de.vanita5.twittnuker.util.ParseUtils;
import de.vanita5.twittnuker.util.ThemeUtils;

public class GoogleMapViewerActivity extends BaseSupportActivity implements Constants {

	@Override
	public int getThemeResourceId() {
		return ThemeUtils.getViewerThemeResource(this);
	}

	@Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_google_maps_viewer, menu);
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
				final Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
				if (!(fragment instanceof IMapFragment)) {
					break;
				}
				((IMapFragment) fragment).center();
				break;
			}
		}
		return true;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Uri uri = getIntent().getData();
		if (uri == null || !AUTHORITY_MAP.equals(uri.getAuthority())) {
			finish();
			return;
		}
		final Bundle bundle = new Bundle();
		final double latitude = ParseUtils.parseDouble(uri.getQueryParameter(QUERY_PARAM_LAT), Double.NaN);
		final double longitude = ParseUtils.parseDouble(uri.getQueryParameter(QUERY_PARAM_LNG), Double.NaN);
		if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
			finish();
			return;
		}
		try {
			bundle.putDouble(EXTRA_LATITUDE, latitude);
			bundle.putDouble(EXTRA_LONGITUDE, longitude);
		} catch (final NumberFormatException e) {
			finish();
			return;
		}
		final ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		final Fragment fragment = isNativeMapSupported() ? new GoogleMapFragment() : new WebMapFragment();
		fragment.setArguments(bundle);
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(android.R.id.content, fragment).commit();
	}

	private boolean isNativeMapSupported() {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
	}
}