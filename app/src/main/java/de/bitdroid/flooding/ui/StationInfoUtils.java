package de.bitdroid.flooding.ui;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.Measurement;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.utils.StringUtils;

/**
 * Helper class for displaying station info on cards.
 */
public class StationInfoUtils {

	private final Context context;

	@Inject
	StationInfoUtils(Context context) {
		this.context = context;
	}


	public void setupStationCards(
			StationMeasurements measurements,
			CardView levelsCard,
			CardView charValuesCard,
			CardView metadataCard,
			CardView mapCard) {

		setupLevelCard(measurements, levelsCard);
		setupCharValuesCard(measurements, charValuesCard);
		setupMetadataCard(measurements, metadataCard);
		setupMapCard(measurements, mapCard);
		if (!hasCharValues(measurements)) charValuesCard.setVisibility(View.GONE);
	}


	private void setupLevelCard(StationMeasurements measurements, CardView card) {
		TextView timestampView = (TextView) card.findViewById(R.id.timestamp);
		TextView levelView = (TextView) card.findViewById(R.id.level);
		Date date = new Date(measurements.getLevelTimestamp());
		timestampView.setText(DateFormat.getDateFormat(context).format(date) + " " + DateFormat.getTimeFormat(context).format(date));
		if (measurements.getLevel() != null) {
			levelView.setText(measurements.getLevel().getValue() + " " + measurements.getLevel().getUnit());
		}
	}


	private void setupCharValuesCard(StationMeasurements measurements, CardView card) {
		setupChardValues((TextView) card.findViewById(R.id.mhw), measurements.getMhw());
		setupChardValues((TextView) card.findViewById(R.id.mnw), measurements.getMnw());
		setupChardValues((TextView) card.findViewById(R.id.mw), measurements.getMw());
		setupChardValues((TextView) card.findViewById(R.id.mthw), measurements.getMthw());
		setupChardValues((TextView) card.findViewById(R.id.mtnw), measurements.getMtnw());
		setupChardValues((TextView) card.findViewById(R.id.hthw), measurements.getHthw());
		setupChardValues((TextView) card.findViewById(R.id.ntnw), measurements.getNtnw());
	}


	private void setupChardValues(TextView valueView, Measurement measurement) {
		if (measurement != null) {
			valueView.setText(measurement.getValue() + " " + measurement.getUnit());
		} else {
			((TableRow) valueView.getParent()).setVisibility(View.GONE);
		}
	}


	private boolean hasCharValues(StationMeasurements measurements) {
		return measurements.getMhw() != null || measurements.getMnw() != null || measurements.getMw() != null
				|| measurements.getMthw() != null || measurements.getMtnw() != null
				|| measurements.getHthw() != null || measurements.getNtnw() != null;
	}


	private void setupMetadataCard(StationMeasurements measurements, CardView card) {
		TextView nameView = (TextView) card.findViewById(R.id.station);
		TextView waterView = (TextView) card.findViewById(R.id.water);
		TextView riverKmView = (TextView) card.findViewById(R.id.riverKm);
		TextView coordinatesView = (TextView) card.findViewById(R.id.coordinates);
		TextView zeroView = (TextView) card.findViewById(R.id.zero);

		Station station = measurements.getStation();
		nameView.setText(StringUtils.toProperCase(station.getStationName()));
		waterView.setText(StringUtils.toProperCase(station.getBodyOfWater().getName()));
		if (station.getRiverKm() != null) riverKmView.setText(String.valueOf(station.getRiverKm()));
		else hideParentView(riverKmView);
		if (station.getLatitude() != null && station.getLongitude() != null) {
			coordinatesView.setText(station.getLatitude() + ", " + station.getLongitude());
		} else {
			hideParentView(coordinatesView);
		}
		if (measurements.getLevelZero() != null) {
			zeroView.setText(measurements.getLevelZero().getValue() + measurements.getLevelZero().getUnit());
		} else {
			hideParentView(zeroView);
		}
	}


	private void setupMapCard(StationMeasurements measurements, CardView card) {
		MapView mapView = (MapView) card.findViewById(R.id.map);

		List<Station> stationList = new ArrayList<>();
		stationList.add(measurements.getStation());
		StationsOverlay overlay = new StationsOverlay(
				context,
				stationList,
				null);
		mapView.getOverlays().add(overlay);

		GeoPoint center;
		if (measurements.getStation().getLatitude() != null && measurements.getStation().getLongitude() != null) {
			center = new GeoPoint(measurements.getStation().getLatitude(), measurements.getStation().getLongitude());
		} else {
			center = new GeoPoint(0, 0);
		}
		mapView.getController().setCenter(center);
		mapView.getController().setZoom(9);
		mapView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}


	private void hideParentView(View view) {
		((View) view.getParent()).setVisibility(View.GONE);
	}

}
