package de.bitdroid.flooding.ui;

import android.os.Bundle;
import android.support.v7.widget.CardView;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;
import timber.log.Timber;


/**
 * Adds a new alarm.
 */
@ContentView(R.layout.activity_new_alarm)
public class NewAlarmActivity extends AbstractActivity {

	@InjectView(R.id.card_levels) private CardView levelsCard;
	@InjectView(R.id.card_char_values) private CardView charValuesCard;
	@InjectView(R.id.card_metadata) private CardView metadataCard;
	@InjectView(R.id.card_map) private CardView mapCard;

	@Inject private NetworkUtils networkUtils;
	@Inject private OdsManager odsManager;
	@Inject private StationInfoUtils stationInfoUtils;

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
						NewAlarmActivity.this.measurements = stationMeasurements;
						stationInfoUtils.setupStationCards(measurements, levelsCard, charValuesCard, metadataCard, mapCard);
					}
				}, new Action1<Throwable>() {
					@Override
					public void call(Throwable throwable) {
						Timber.e(throwable, "failed to download measurements");
					}
				});
	}


}
