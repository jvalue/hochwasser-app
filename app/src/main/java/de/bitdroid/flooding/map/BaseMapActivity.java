package de.bitdroid.flooding.map;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;
import de.bitdroid.utils.StringUtils;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LAT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LONG;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

public abstract class BaseMapActivity extends Activity implements Extras {

	private static final int LOADER_ID = 43;
	
	private MyLocationNewOverlay locationOverlay;
	private FixedMapView mapView;
	private StationsOverlay stationsOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
		final String waterName = getIntent().getStringExtra(EXTRA_WATER_NAME);
		final String stationName = getIntent().getStringExtra(EXTRA_STATION_NAME);

		// enable action bar back button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		if (waterName != null) setTitle(StringUtils.toProperCase(waterName));
		else if (stationName != null) setTitle(StringUtils.toProperCase(stationName));

		// map view
		mapView = (FixedMapView) findViewById(R.id.map);
		mapView.setMultiTouchControls(true);

		// location overlay
		locationOverlay = new MyLocationNewOverlay(
				getApplicationContext(), 
				new GpsMyLocationProvider(getApplicationContext()), 
				mapView);
		mapView.getOverlays().add(locationOverlay);
		
		// load station data
		AbstractLoaderCallbacks loaderCallback = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				cursor.moveToFirst();
				if (cursor.getCount() == 0) return;

				List<Station> stations = new ArrayList<Station>();

				int latIdx = cursor.getColumnIndex(COLUMN_STATION_LAT);
				int longIdx = cursor.getColumnIndex(COLUMN_STATION_LONG);
				int nameIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);

				do {
					String stationName = cursor.getString(nameIdx);
					double lat = cursor.getDouble(latIdx);
					double lon = cursor.getDouble(longIdx);

					stations.add(new Station(stationName, lat, lon));
				} while (cursor.moveToNext());

				// filter stations with invalid coordinates
				filterInvalidStations(stations);

				// add to overlays
				if (stationsOverlay != null) mapView.getOverlays().remove(stationsOverlay);
				stationsOverlay = new StationsOverlay(
						getApplicationContext(), 
						stations,
						getStationClickListener());
				mapView.getOverlays().add(stationsOverlay);

				GeoPoint  point = getCenter(stations);
				mapView.getController().setCenter(point);
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
				} else if (stationName != null) {
					selection = COLUMN_STATION_NAME + "=?";
					selectionParams = new String[] { stationName };
				}

				return new CursorLoader(
						getApplicationContext(),
						PegelOnlineSource.INSTANCE.toUri(),
						new String[] {
							COLUMN_STATION_LAT, 
							COLUMN_STATION_LONG, 
							COLUMN_STATION_NAME,
						},
						selection, selectionParams, null);
			}
		};
		getLoaderManager().initLoader(
				StationsOverlay.LOADER_ID,
				null,
				loaderCallback);
    }


	@Override
	public void onResume() {
		super.onResume();
		locationOverlay.enableMyLocation();
	}

	
	@Override
	public void onPause() {
		super.onPause();
		locationOverlay.disableMyLocation();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case android.R.id.home:
				finish();
				overridePendingTransition(R.anim.slide_enter_from_left, R.anim.slide_exit_to_right);
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_enter_from_left, R.anim.slide_exit_to_right);
	}


	private static final String
		EXTRA_SCROLL_X = "EXTRA_SCROLL_X",
		EXTRA_SCROLL_Y = "EXTRA_SCROLL_Y",
		EXTRA_ZOOM_LEVEL = "EXTRA_ZOOM_LEVEL";

	@Override
	protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		state.putInt(EXTRA_SCROLL_X, mapView.getScrollX());
		state.putInt(EXTRA_SCROLL_Y, mapView.getScrollY());
		state.putInt(EXTRA_ZOOM_LEVEL, mapView.getZoomLevel());
	}


	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		mapView.getController().setZoom(state.getInt(EXTRA_ZOOM_LEVEL, 1));
		mapView.scrollTo(
				state.getInt(EXTRA_SCROLL_X, 0),
				state.getInt(EXTRA_SCROLL_Y, 0));
	}


	protected abstract StationClickListener getStationClickListener();

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
