package de.bitdroid.flooding.map;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

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
import de.bitdroid.flooding.utils.BaseActivity;
import de.bitdroid.utils.StringUtils;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LAT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LONG;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

public abstract class BaseMapActivity extends BaseActivity implements Extras {

	private static final int LOADER_ID = 43;
	
	private MyLocationNewOverlay locationOverlay;
	private FixedMapView mapView;
	private StationsOverlay stationsOverlay;

	protected String waterName, stationName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

		waterName = getIntent().getStringExtra(EXTRA_WATER_NAME);
		stationName = getIntent().getStringExtra(EXTRA_STATION_NAME);

		// enable action bar back button
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
				int riverIdx = cursor.getColumnIndex(COLUMN_WATER_NAME);

				do {
					String stationName = cursor.getString(nameIdx);
					double lat = cursor.getDouble(latIdx);
					double lon = cursor.getDouble(longIdx);
					String riverName = cursor.getString(riverIdx);

					stations.add(new Station(stationName, riverName, lat, lon));
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
				if (waterName != null || stationName != null) mapView.getController().setZoom(8);
				else mapView.getController().setZoom(7);
			}

			@Override
			protected void onLoaderResetHelper(Loader<Cursor> loader) { }

			@Override
			protected Loader<Cursor> getCursorLoader() {
				String selection = COLUMN_LEVEL_TYPE + "=?";
				String[] selectionParams = null;
				if (waterName != null) {
					selection += " AND " + COLUMN_WATER_NAME + "=?";
					selectionParams = new String[] { "W", waterName };
				} else if (stationName != null) {
					selection += " AND " + COLUMN_STATION_NAME + "=?";
					selectionParams = new String[] { "W", stationName };
				} else {
					selectionParams = new String[] { "W" };
				}

				return new CursorLoader(
						getApplicationContext(),
						PegelOnlineSource.INSTANCE.toUri(),
						new String[] {
							COLUMN_STATION_LAT, 
							COLUMN_STATION_LONG, 
							COLUMN_STATION_NAME,
							COLUMN_WATER_NAME
						},
						selection, selectionParams, null);
			}
		};
		getSupportLoaderManager().initLoader(
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
