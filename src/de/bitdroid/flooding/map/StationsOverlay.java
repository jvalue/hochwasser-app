package de.bitdroid.flooding.map;

import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LAT;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_LONG;
import static de.bitdroid.flooding.pegelonline.PegelOnlineSource.COLUMN_STATION_NAME;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Point;

import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.utils.AbstractLoaderCallbacks;


final class StationsOverlay extends ItemizedOverlay<OverlayItem> {
	
	public static final int LOADER_ID = 43;

	private final AbstractLoaderCallbacks loaderCallback;
	private final List<OverlayItem> items = new ArrayList<OverlayItem>();

	public StationsOverlay(final Context context) {
		super(
				context.getResources().getDrawable(android.R.drawable.presence_online), 
				new ResourceProxyImpl(context));

		this.loaderCallback = new AbstractLoaderCallbacks(LOADER_ID) {

			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				cursor.moveToFirst();
				int latIdx = cursor.getColumnIndex(COLUMN_STATION_LAT);
				int longIdx = cursor.getColumnIndex(COLUMN_STATION_LONG);
				int nameIdx = cursor.getColumnIndex(COLUMN_STATION_NAME);

				while (cursor.moveToNext()) {
					GeoPoint point = new GeoPoint(
							cursor.getDouble(latIdx),
							cursor.getDouble(longIdx));
					String stationName = cursor.getString(nameIdx);

					items.add(new OverlayItem(stationName, stationName, point));
				}
				populate();
			}

			@Override
			protected void onLoaderResetHelper(Loader<Cursor> loader) { }

			@Override
			protected Loader<Cursor> getCursorLoader() {
				return new CursorLoader(
						context,
						PegelOnlineSource.INSTANCE.toUri(),
						new String[] {
							COLUMN_STATION_LAT, 
							COLUMN_STATION_LONG, 
							COLUMN_STATION_NAME 
						}, null, null, null);
			}
		};
	}


	@Override
	protected OverlayItem createItem(int idx) {
		return items.get(idx);
	}


	@Override
	public int size() {
		return items.size();
	}


	@Override
	public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
		return false;
	}


	public AbstractLoaderCallbacks getLoaderCallback() {
		return loaderCallback;
	}
}
