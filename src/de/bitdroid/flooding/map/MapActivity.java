package de.bitdroid.flooding.map;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_KM;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LAT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LONG;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;

public class MapActivity extends Activity implements MapConstants {

	public static final String EXTRA_WATER_NAME = "EXTRA_WATER_NAME";

	private static final int LOADER_ID = 43;
	
	private MyLocationNewOverlay locationOverlay;
	private FixedMapView mapView;
	private StationsOverlay stationsOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
		final String waterName = getIntent().getStringExtra(EXTRA_WATER_NAME);

		// map view
		mapView = (FixedMapView) findViewById(R.id.map);
		mapView.setMultiTouchControls(true);

		// location overlay
		locationOverlay = new MyLocationNewOverlay(
				getApplicationContext(), 
				new GpsMyLocationProvider(getApplicationContext()), 
				mapView);
		mapView.getOverlays().add(locationOverlay);
		
		// locad station data
		AbstractLoaderCallbacks loaderCallback = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				cursor.moveToFirst();
				if (cursor.getCount() == 0) return;

				List<Station> stations = new ArrayList<Station>();

				int latIdx = cursor.getColumnIndex(COLUMN_STATION_LAT);
				int longIdx = cursor.getColumnIndex(COLUMN_STATION_LONG);
				int nameIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);
				int kmIdx = cursor.getColumnIndex(COLUMN_STATION_KM);

				do {
					String stationName = cursor.getString(nameIdx);
					double km = cursor.getDouble(kmIdx);
					double lat = cursor.getDouble(latIdx);
					double lon = cursor.getDouble(longIdx);

					stations.add(new Station(stationName, km, lat, lon));
				} while (cursor.moveToNext());

				// filter stations with invaild cooridnates
				filterInvalidStations(stations);

				// add to overlays
				if (stationsOverlay != null) mapView.getOverlays().remove(stationsOverlay);
				stationsOverlay = new StationsOverlay(
						getApplicationContext(), 
						MapActivity.this, 
						stations);
				mapView.getOverlays().add(stationsOverlay);

				Point center = mapView.getProjection().toPixels(getCenter(stations), null);
				mapView.scrollTo(center.x, center.y);
				mapView.getController().setZoom(8);
			}

			@Override
			protected void onLoaderResetHelper(Loader<Cursor> loader) { }

			@Override
			protected Loader<Cursor> getCursorLoader() {
				String selection = null;
				String[] selectionParams = null;
				if (waterName != null) {
					selection = COLUMN_WATER_NAME + "=?";
					selectionParams = new String[] { waterName };
				}

				return new CursorLoader(
						getApplicationContext(),
						PegelOnlineSource.INSTANCE.toUri(),
						new String[] {
							COLUMN_STATION_LAT, 
							COLUMN_STATION_LONG, 
							COLUMN_STATION_NAME,
							COLUMN_STATION_KM
						}, 
						selection, selectionParams, null);
			}
		};
		getLoaderManager().initLoader(
				StationsOverlay.LOADER_ID,
				null,
				loaderCallback);

		// restore map orientation
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
		/*
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mapView.getController().setZoom(prefs.getInt(PREFS_ZOOM_LEVEL, 1));
		mapView.scrollTo(
				prefs.getInt(PREFS_SCROLL_X, 0),
				prefs.getInt(PREFS_SCROLL_Y, 0));

		Toast.makeText(this, "Zoom = " + mapView.getZoomLevel(), Toast.LENGTH_LONG).show();
		Toast.makeText(this, "Loc = " + mapView.getScrollX() + ", " + mapView.getScrollY(), Toast.LENGTH_LONG).show();
		*/
	}


	private GeoPoint getCenter(List<Station> stations) {
		double 
			minX = Double.MAX_VALUE,
			maxX = Double.MIN_VALUE,
			minY = Double.MAX_VALUE,
			maxY = Double.MIN_VALUE;

		for (Station station : stations) {
			minX = Math.min(station.getLat(), minX);
			maxX = Math.max(station.getLat(), maxX);
			minY = Math.min(station.getLon(), minY);
			maxY = Math.max(station.getLon(), maxY);
		}

		return new GeoPoint((minX + maxX) / 2.0f, (minY + maxY) / 2.0f);
	}


	private void filterInvalidStations(List<Station> stations) {
		Iterator<Station> iter = stations.iterator();
		while (iter.hasNext()) {
			Station s = iter.next();
			if (s.getLat() == 0 && s.getLon() == 0) iter.remove();
		}
	}

}
