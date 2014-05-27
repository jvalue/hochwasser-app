package de.bitdroid.flooding.map;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Point;

import de.bitdroid.flooding.StationsLoaderCallbacks;
import de.bitdroid.flooding.ods.OdsContract;
import de.bitdroid.flooding.ods.json.PegelonlineParser;
import de.bitdroid.flooding.utils.Log;


final class StationsOverlay extends ItemizedOverlay<OverlayItem> {

	private final StationsLoaderCallbacks loaderCallback;
	private final List<OverlayItem> items = new ArrayList<OverlayItem>();

	public StationsOverlay(Context context) {
		super(
				context.getResources().getDrawable(android.R.drawable.presence_online), 
				new ResourceProxyImpl(context));

		this.loaderCallback = new StationsLoaderCallbacks(context) {
			@Override
			protected void onLoadFinishedHelper(Loader<Cursor> loader, Cursor cursor) {
				synchronized(items) {
					cursor.moveToFirst();
					while (cursor.moveToNext()) {
						/*
						int idx = cursor.getColumnIndex(OdsContract.COLUMN_JSON_DATA);
						try {
							JSONObject json = new JSONObject(cursor.getString(idx));
							String stationName = PegelonlineParser.getStationName(json);
							GeoPoint point = new GeoPoint(
									PegelonlineParser.getLatitude(json),
									PegelonlineParser.getLongitude(json));
							items.add(new OverlayItem(stationName, stationName, point));
						} catch (Exception je) {
							Log.error(android.util.Log.getStackTraceString(je));
						}
						*/
					}
					populate();
				}
			}
			@Override
			protected void onLoaderResetHelper(Loader<Cursor> loader) { }
		};
	}


	@Override
	protected OverlayItem createItem(int idx) {
		synchronized(items) {
			return items.get(idx);
		}
	}


	@Override
	public int size() {
		synchronized(items) {
			return items.size();
		}
	}


	@Override
	public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
		return false;
	}


	public StationsLoaderCallbacks getLoaderCallback() {
		return loaderCallback;
	}
}
