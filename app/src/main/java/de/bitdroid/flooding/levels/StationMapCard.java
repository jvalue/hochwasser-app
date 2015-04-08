package de.bitdroid.flooding.levels;

import android.annotation.TargetApi;
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
import de.bitdroid.flooding.map.InfoMapActivity;
import de.bitdroid.flooding.map.Station;
import de.bitdroid.flooding.map.StationClickListener;
import de.bitdroid.flooding.map.StationsOverlay;
import it.gmariotti.cardslib.library.internal.Card;

public final class StationMapCard extends Card {

	private final String stationName;
	private final String riverName;
	private final Double lat, lon;
	private final Activity context;

	public StationMapCard(
			Activity context,
			String staionName,
			String riverName,
			Double lat,
			Double lon) {

		super(context.getApplicationContext(), R.layout.station_card_map);
		this.context = context;
		this.stationName = staionName;
		this.riverName = riverName;
		this.lat = lat;
		this.lon = lon;
	}


	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setupInnerViewElements(ViewGroup parent, View view) {
		if (isEmpty()) return;

		final ClickableMapView mapView = (ClickableMapView) view.findViewById(R.id.map);

		// start full map on click (not much fun zoom / panning on a small card)
		mapView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, InfoMapActivity.class);
				intent.putExtra(InfoMapActivity.EXTRA_WATER_NAME, riverName);
				intent.putExtra(InfoMapActivity.EXTRA_STATION_NAME, stationName);
				context.startActivity(intent);
				context.overridePendingTransition(
						R.anim.slide_enter_from_right,
						R.anim.slide_exit_to_left);
			}
		});

		// station overlay
		List<Station> station = new LinkedList<Station>();
		station.add(new Station(stationName, riverName, lat, lon));
		StationsOverlay stationsOverlay = new StationsOverlay(
				getContext(),
				station,
				new StationClickListener() {
					@Override
					public void onStationClicked(Station station) {  }
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
