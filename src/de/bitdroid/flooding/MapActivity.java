package de.bitdroid.flooding;

import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MapActivity extends Activity {
	
	private MyLocationNewOverlay locationOverlay;
	private FixedMapView mapView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

		mapView = (FixedMapView) findViewById(R.id.map);
		mapView.setMultiTouchControls(true);

		locationOverlay = new MyLocationNewOverlay(this, new GpsMyLocationProvider(this), mapView);
		mapView.getOverlays().add(locationOverlay);

		restoreMapState();
    }


	@Override
	public void onStop() {

		saveMapState();
		super.onStop();
	}


	@Override
	public void onResume() {
		locationOverlay.enableMyLocation();

		super.onResume();
	}

	
	@Override
	public void onPause() {
		locationOverlay.disableMyLocation();

		super.onPause();
	}



	private void saveMapState() {
		SharedPreferences.Editor editor = getSharedPreferences(OpenStreetMapConstants.PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putInt(OpenStreetMapConstants.PREFS_SCROLL_X, mapView.getScrollX());
		editor.putInt(OpenStreetMapConstants.PREFS_SCROLL_Y, mapView.getScrollY());
		editor.putInt(OpenStreetMapConstants.PREFS_ZOOM_LEVEL, mapView.getZoomLevel());
		editor.commit();
	}

	private void restoreMapState() {
		SharedPreferences prefs = getSharedPreferences(OpenStreetMapConstants.PREFS_NAME, Context.MODE_PRIVATE);
		mapView.getController().setZoom(prefs.getInt(OpenStreetMapConstants.PREFS_ZOOM_LEVEL, 1));
		mapView.scrollTo(
				prefs.getInt(OpenStreetMapConstants.PREFS_SCROLL_X, 0),
				prefs.getInt(OpenStreetMapConstants.PREFS_SCROLL_Y, 0));
	}

}
