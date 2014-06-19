package de.bitdroid.flooding.map;

import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import de.bitdroid.flooding.R;

public class MapActivity extends Activity implements MapConstants {

	public static final String EXTRA_WATER_NAME = "EXTRA_WATER_NAME";
	
	private MyLocationNewOverlay locationOverlay;
	private FixedMapView mapView;
	private StationsOverlay stationsOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

		String waterName = getIntent().getStringExtra(EXTRA_WATER_NAME);

		mapView = (FixedMapView) findViewById(R.id.map);
		mapView.setMultiTouchControls(true);

		locationOverlay = new MyLocationNewOverlay(
				getApplicationContext(), 
				new GpsMyLocationProvider(getApplicationContext()), 
				mapView);
		mapView.getOverlays().add(locationOverlay);
		
		stationsOverlay = new StationsOverlay(getApplicationContext(), waterName);
		mapView.getOverlays().add(stationsOverlay);

		getLoaderManager().initLoader(
				StationsOverlay.LOADER_ID,
				null,
				stationsOverlay.getLoaderCallback());

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
		SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
		editor.putInt(PREFS_SCROLL_X, mapView.getScrollX());
		editor.putInt(PREFS_SCROLL_Y, mapView.getScrollY());
		editor.putInt(PREFS_ZOOM_LEVEL, mapView.getZoomLevel());
		editor.commit();
	}

	private void restoreMapState() {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mapView.getController().setZoom(prefs.getInt(PREFS_ZOOM_LEVEL, 1));
		mapView.scrollTo(
				prefs.getInt(PREFS_SCROLL_X, 0),
				prefs.getInt(PREFS_SCROLL_Y, 0));
	}

}
