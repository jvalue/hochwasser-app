package de.bitdroid.flooding.ui;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Date;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.ods.Measurement;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;


/**
 * Displays information about a single station.
 */
@ContentView(R.layout.activity_station_info)
public class StationInfoActivity extends AbstractActivity {

	@InjectView(R.id.card_levels) private CardView levelsCard;
	@InjectView(R.id.timestamp) private TextView timestampView;
	@InjectView(R.id.level) private TextView levelView;

	@InjectView(R.id.card_char_values) private CardView charValuesCard;
	@InjectView(R.id.mhw) private TextView mhwView;
	@InjectView(R.id.mnw) private TextView mnwView;
	@InjectView(R.id.mw) private TextView mwView;
	@InjectView(R.id.mthw) private TextView mthwView;
	@InjectView(R.id.mtnw) private TextView mtnwView;
	@InjectView(R.id.hthw) private TextView hthwView;
	@InjectView(R.id.ntnw) private TextView ntnwView;

	@InjectView(R.id.card_metadata) private CardView metadataCard;
	@InjectView(R.id.station) private TextView nameView;
	@InjectView(R.id.water) private TextView waterView;
	@InjectView(R.id.riverKm) private TextView riverKmView;
	@InjectView(R.id.coordinates) private TextView coordinatesView;
	@InjectView(R.id.zero) private TextView zeroView;

	@InjectView(R.id.card_map) private CardView mapCard;
	@InjectView(R.id.card_more) private CardView moreCard;

	@Inject private NetworkUtils networkUtils;
	@Inject private OdsManager odsManager;

	private StationMeasurements measurements;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// set title
		Station station = new StationSelection(getIntent()).getStation();
		getSupportActionBar().setTitle(StringUtils.toProperCase(station.getStationName()));
		getSupportActionBar().setSubtitle(StringUtils.toProperCase(station.getBodyOfWater().getName()));

		// load data
		odsManager.getMeasurements(station)
				.compose(networkUtils.<StationMeasurements>getDefaultTransformer())
				.subscribe(new Action1<StationMeasurements>() {
					@Override
					public void call(StationMeasurements stationMeasurements) {
						StationInfoActivity.this.measurements = stationMeasurements;
						setupLevelCard();
						setupCharValuesCard();
						setupMetadataCard();
						if (!hasCharValues()) charValuesCard.setVisibility(View.GONE);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "failed to download measurements");
					}
				});
	}


	private void setupLevelCard() {
		Date date = new Date(measurements.getLevelTimestamp());
		timestampView.setText(DateFormat.getDateFormat(this).format(date) + " " + DateFormat.getTimeFormat(this).format(date));
		levelView.setText(measurements.getLevel().getValue() + " " + measurements.getLevel().getUnit());
	}


	private void setupCharValuesCard() {
		setupChardValues(mhwView, measurements.getMhw());
		setupChardValues(mnwView, measurements.getMnw());
		setupChardValues(mwView, measurements.getMw());
		setupChardValues(mthwView, measurements.getMthw());
		setupChardValues(mtnwView, measurements.getMtnw());
		setupChardValues(hthwView, measurements.getHthw());
		setupChardValues(ntnwView, measurements.getNtnw());
	}


	private void setupChardValues(TextView valueView, Measurement measurement) {
		if (measurement != null) {
			valueView.setText(measurement.getValue() + " " + measurement.getUnit());
		} else {
			((TableRow) valueView.getParent()).setVisibility(View.GONE);
		}
	}


	private boolean hasCharValues() {
		return measurements.getMhw() != null || measurements.getMnw() != null || measurements.getMw() != null
				|| measurements.getMthw() != null || measurements.getMtnw() != null
				|| measurements.getHthw() != null || measurements.getNtnw() != null;
	}


	private void setupMetadataCard() {
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


	private void hideParentView(View view) {
		((View) view.getParent()).setVisibility(View.GONE);
	}

}
