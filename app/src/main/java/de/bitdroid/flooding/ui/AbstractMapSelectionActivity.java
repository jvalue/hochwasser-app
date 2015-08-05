package de.bitdroid.flooding.ui;

import android.os.Bundle;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.network.DefaultErrorAction;
import de.bitdroid.flooding.network.ErrorActionBuilder;
import de.bitdroid.flooding.network.HideSpinnerAction;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;


/**
 * Select one station from a map.
 */
@ContentView(R.layout.activity_select_map)
public abstract class AbstractMapSelectionActivity extends AbstractRestrictedActivity implements StationClickListener {

	private static final String
			EXTRA_SCROLL_X = "EXTRA_SCROLL_X",
			EXTRA_SCROLL_Y = "EXTRA_SCROLL_Y",
			EXTRA_ZOOM_LEVEL = "EXTRA_ZOOM_LEVEL";


	@InjectView(R.id.map) private FixedMapView mapView;
	private MyLocationNewOverlay locationOverlay;
	private StationsOverlay stationsOverlay;

	@Inject private NetworkUtils networkUtils;
	@Inject private OdsManager odsManager;

	private StationSelection stationSelection;

	public abstract void onStationClicked(Station station);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.select_station);
		analyticsUtils.onScreen("map selection screen");

		// load args
		stationSelection = new StationSelection(getIntent());

		// setup map
		mapView.setMultiTouchControls(true);
		locationOverlay = new MyLocationNewOverlay(
				this,
				new GpsMyLocationProvider(this),
				mapView);
		mapView.getOverlays().add(locationOverlay);

		// restore state
		if (savedInstanceState != null) {
			mapView.getController().setZoom(savedInstanceState.getInt(EXTRA_ZOOM_LEVEL, 1));
			mapView.scrollTo(
					savedInstanceState.getInt(EXTRA_SCROLL_X, 0),
					savedInstanceState.getInt(EXTRA_SCROLL_Y, 0));
		}

		loadData();
    }


	@Override
	public void onResume() {
		super.onResume();
		locationOverlay.enableMyLocation();
	}

	
	@Override
	public void onPause() {
		locationOverlay.disableMyLocation();
		super.onPause();
	}


	private void loadData() {
		Observable<List<Station>> stationsObservable;
		if (stationSelection.getWater() == null) stationsObservable = odsManager.getStations();
		else stationsObservable = odsManager.getStationsByBodyOfWater(stationSelection.getWater());

		compositeSubscription.add(stationsObservable
				.flatMap(new Func1<List<Station>, Observable<List<Station>>>() {
					@Override
					public Observable<List<Station>> call(List<Station> stations) {
						Iterator<Station> iter = stations.iterator();
						while (iter.hasNext()) {
							Station s = iter.next();
							if (s.getLatitude() == null && s.getLongitude() == null) iter.remove();
						}
						return Observable.just(stations);
					}
				})
				.compose(networkUtils.<List<Station>>getDefaultTransformer())
				.subscribe(new Action1<List<Station>>() {
					@Override
					public void call(List<Station> stations) {
						if (stationsOverlay != null) mapView.getOverlays().remove(stationsOverlay);
						stationsOverlay = new StationsOverlay(
								AbstractMapSelectionActivity.this,
								stations,
								AbstractMapSelectionActivity.this);
						mapView.getOverlays().add(stationsOverlay);

						GeoPoint point = getCenter(stations);
						mapView.getController().setCenter(point);
						if (stationSelection.getWater() == null) mapView.getController().setZoom(8);
						else mapView.getController().setZoom(7);
					}
				}, new ErrorActionBuilder()
						.add(new DefaultErrorAction(this, this, "failed to download stations"))
						.add(new HideSpinnerAction(this))
						.build()));
	}



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
			minX = Math.min(station.getLatitude(), minX);
			maxX = Math.max(station.getLatitude(), maxX);
			minY = Math.min(station.getLongitude(), minY);
			maxY = Math.max(station.getLongitude(), maxY);
		}

		return new GeoPoint((minX + maxX) / 2.0f, (minY + maxY) / 2.0f);
	}

}
