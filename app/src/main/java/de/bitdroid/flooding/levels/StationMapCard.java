package de.bitdroid.flooding.levels;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.osmdroid.util.GeoPoint;

import java.util.LinkedList;
import java.util.List;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.map.FixedMapView;
import de.bitdroid.flooding.map.Station;
import de.bitdroid.flooding.map.StationClickListener;
import de.bitdroid.flooding.map.StationsOverlay;
import it.gmariotti.cardslib.library.internal.Card;

final class StationMapCard extends Card {

	private String name;
	private Double lat, lon;

	public StationMapCard(
			Context context,
			String name,
			Double lat,
			Double lon) {

		super(context, R.layout.station_card_map);
		this.name = name;
		this.lat = lat;
		this.lon = lon;
	}


	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		if (isEmpty()) return;

		final FixedMapView mapView = (FixedMapView) view.findViewById(R.id.map);

		// station overlay
		List<Station> station = new LinkedList<Station>();
		station.add(new Station(name, lat, lon));
		StationsOverlay stationsOverlay = new StationsOverlay(
				getContext(),
				station,
				new StationClickListener() {
					@Override
					public void onStationClick(Station station) {  }
				});
		mapView.getOverlays().add(stationsOverlay);

		// hack to center map view before width / height are known
		final ViewTreeObserver observer = mapView.getViewTreeObserver();
		ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mapView.getController().setZoom(8);
				mapView.getController().setCenter(new GeoPoint(lat, lon));

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				} else {
					mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				}
			}
		};
		observer.addOnGlobalLayoutListener(listener);
	}


	public boolean isEmpty() {
		return lat == null && lon == null;
	}

}
