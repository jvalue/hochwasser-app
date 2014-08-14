package de.bitdroid.flooding.map;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_LEVEL_TYPE;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LAT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LONG;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

public abstract class BaseMapFragment extends Fragment implements StationClickListener, Extras {

	private static final int LOADER_ID = 43;

	public abstract void onStationClicked(Station station);

	protected static void setArguments(
			BaseMapFragment mapFragment,
			String waterName,
			String stationName) {

		Bundle extras = new Bundle();
		extras.putString(EXTRA_WATER_NAME, waterName);
		extras.putString(EXTRA_STATION_NAME, stationName);
		mapFragment.setArguments(extras);
	}


	private MyLocationNewOverlay locationOverlay;
	private FixedMapView mapView;
	private StationsOverlay stationsOverlay;

	protected String waterName, stationName;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.map, container, false);

		waterName = getArguments().getString(EXTRA_WATER_NAME);
		stationName = getArguments().getString(EXTRA_STATION_NAME);

		// map view
		mapView = (FixedMapView) view.findViewById(R.id.map);
		mapView.setMultiTouchControls(true);

		// location overlay
		locationOverlay = new MyLocationNewOverlay(
				getActivity().getApplicationContext(),
				new GpsMyLocationProvider(getActivity().getApplicationContext()),
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
						getActivity().getApplicationContext(),
						stations,
						BaseMapFragment.this);
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
				String[] selectionParams;
				if (waterName == null && stationName == null) {
					selectionParams = new String[]{"W"};
				} else if (stationName != null) {
					selection += " AND " + COLUMN_STATION_NAME + "=?";
					selectionParams = new String[]{"W", stationName};
				} else {
					selection += " AND " + COLUMN_WATER_NAME + "=?";
					selectionParams = new String[] { "W", waterName };
				}

				return new CursorLoader(
						getActivity().getApplicationContext(),
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
		getLoaderManager().initLoader(
				StationsOverlay.LOADER_ID,
				null,
				loaderCallback);

		return view;
    }


	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);

		// restore state
		if (state != null) {
			mapView.getController().setZoom(state.getInt(EXTRA_ZOOM_LEVEL, 1));
			mapView.scrollTo(
					state.getInt(EXTRA_SCROLL_X, 0),
					state.getInt(EXTRA_SCROLL_Y, 0));
		}
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
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		if (mapView == null) return;
		state.putInt(EXTRA_SCROLL_X, mapView.getScrollX());
		state.putInt(EXTRA_SCROLL_Y, mapView.getScrollY());
		state.putInt(EXTRA_ZOOM_LEVEL, mapView.getZoomLevel());
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
