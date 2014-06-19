package de.bitdroid.flooding.map;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_KM;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LAT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LONG;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_WATER_NAME;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Point;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;


final class StationsOverlay extends ItemizedOverlay<OverlayItem> {
	
	public static final int LOADER_ID = 43;

	private final AbstractLoaderCallbacks loaderCallback;
	private final List<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	private final List<Station> stationItems = new ArrayList<Station>();
	private final Context activityContext;

	public StationsOverlay(
			final Context applicationContext,
			final Context activityContext,
			final String waterName) {
		super(
				applicationContext.getResources().getDrawable(android.R.drawable.presence_online), 
				new ResourceProxyImpl(applicationContext));

		this.activityContext = activityContext;
		this.loaderCallback = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				cursor.moveToFirst();
				if (cursor.getCount() == 0) return;

				int latIdx = cursor.getColumnIndex(COLUMN_STATION_LAT);
				int longIdx = cursor.getColumnIndex(COLUMN_STATION_LONG);
				int nameIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);
				int kmIdx = cursor.getColumnIndex(COLUMN_STATION_KM);

				do {
					String stationName = cursor.getString(nameIdx);
					double km = cursor.getDouble(kmIdx);
					double lat = cursor.getDouble(latIdx);
					double lon = cursor.getDouble(longIdx);

					GeoPoint point = new GeoPoint(lat, lon);
					overlayItems.add(new OverlayItem(stationName, stationName, point));
					stationItems.add(new Station(stationName, km, lat, lon));
				} while (cursor.moveToNext());
				populate();
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
						applicationContext,
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
	}


	@Override
	protected OverlayItem createItem(int idx) {
		return overlayItems.get(idx);
	}


	@Override
	public int size() {
		return overlayItems.size();
	}


	@Override
	public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
		return false;
	}


	@Override
	protected boolean onTap(int index) {
		Station station = stationItems.get(index);
		new AlertDialog.Builder(activityContext)
			.setTitle(R.string.map_dialog_station_info_title)
			.setMessage(activityContext.getString(
						R.string.map_dialog_station_info,
						station.name,
						station.km,
						station.lat,
						station.lon))
			.setPositiveButton(R.string.btn_ok, null)
			.show();

		return true;
	}


	public AbstractLoaderCallbacks getLoaderCallback() {
		return loaderCallback;
	}


	private static class Station {
		String name;
		double lat, lon, km;

		public Station(String name, double km, double lat, double lon) {
			this.name = name;
			this.km = km;
			this.lat = lat;
			this.lon = lon;
		}
	}
		
}
