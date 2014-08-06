package de.bitdroid.flooding.levels;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import org.osmdroid.util.GeoPoint;

import java.util.LinkedList;
import java.util.List;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.map.ClickableMapView;
import de.bitdroid.flooding.map.MapActivity;
import de.bitdroid.flooding.map.Station;
import de.bitdroid.flooding.map.StationClickListener;
import de.bitdroid.flooding.map.StationsOverlay;
import de.bitdroid.utils.Log;
import it.gmariotti.cardslib.library.internal.Card;

final class StationMapCard extends Card {

	private final String name;
	private final Double lat, lon;
	private final Activity context;

	public StationMapCard(
			final Activity context,
			final String name,
			Double lat,
			Double lon) {

		super(context.getApplicationContext(), R.layout.station_card_map);
		this.context = context;
		this.name = name;
		this.lat = lat;
		this.lon = lon;
	}


	@Override
	public void setupInnerViewElements(ViewGroup parent, View view) {
		if (isEmpty()) return;

		final ClickableMapView mapView = (ClickableMapView) view.findViewById(R.id.map);

		// start full map on click (not much fun zoom / panning on a small card)
		mapView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.debug("clicked card!");
				Intent intent = new Intent(context, MapActivity.class);
				intent.putExtra(MapActivity.EXTRA_STATION_NAME, name);
				context.startActivity(intent);
				context.overridePendingTransition(
						R.anim.slide_enter_from_right,
						R.anim.slide_exit_to_left);
			}
		});

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
