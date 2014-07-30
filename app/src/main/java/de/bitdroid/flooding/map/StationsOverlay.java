package de.bitdroid.flooding.map;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;

import de.bitdroid.flooding.R;


final class StationsOverlay extends ItemizedOverlay<OverlayItem> {
	
	public static final int LOADER_ID = 43;

	private final List<OverlayItem> overlayItems = new ArrayList<OverlayItem>();
	private final List<Station> stations = new ArrayList<Station>();
	private final Context context;

	public StationsOverlay(
			Context applicationContext,
			Context activityContext,
			List<Station> stations) {

		super(
				applicationContext.getResources().getDrawable(android.R.drawable.presence_online), 
				new ResourceProxyImpl(applicationContext));

		this.context = activityContext;
		this.stations.addAll(stations);

		for (Station station : stations) {
			String name = station.getName();
			GeoPoint point = new GeoPoint(station.getLat(), station.getLon());
			overlayItems.add(new OverlayItem(name, name, point));
		}

		populate();
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
		Station station = stations.get(index);
		new AlertDialog.Builder(context)
			.setTitle(R.string.map_dialog_station_info_title)
			.setMessage(context.getString(
						R.string.map_dialog_station_info,
						station.getName(),
						station.getKm(),
						station.getLat(),
						station.getLon()))
			.setPositiveButton(R.string.btn_ok, null)
			.show();

		return true;
	}

}
