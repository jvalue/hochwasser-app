package de.bitdroid.flooding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.network.DefaultErrorAction;
import de.bitdroid.flooding.network.ErrorActionBuilder;
import de.bitdroid.flooding.network.HideSpinnerAction;
import de.bitdroid.flooding.ods.OdsManager;
import de.bitdroid.flooding.ods.Station;
import de.bitdroid.flooding.ods.StationMeasurements;
import de.bitdroid.flooding.utils.StringUtils;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import rx.functions.Action1;


/**
 * Displays information about a single station.
 */
@ContentView(R.layout.activity_station_info)
public class StationInfoActivity extends AbstractRestrictedActivity {

	private static final String STATE_MEASUREMENTS = "STATE_MEASUREMENTS";

	@InjectView(R.id.card_levels) private CardView levelsCard;
	@InjectView(R.id.card_char_values) private CardView charValuesCard;
	@InjectView(R.id.card_metadata) private CardView metadataCard;
	@InjectView(R.id.card_map) private CardView mapCard;

	@Inject private OdsManager odsManager;
	@Inject private StationInfoUtils stationInfoUtils;

	private Station station;
	private StationMeasurements measurements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		// set title
		station = new StationSelection(getIntent()).getStation();
		getSupportActionBar().setTitle(StringUtils.toProperCase(station.getStationName()));
		getSupportActionBar().setSubtitle(StringUtils.toProperCase(station.getBodyOfWater().getName()));

		// load data
		if (savedInstanceState != null && savedInstanceState.get(STATE_MEASUREMENTS) != null) {
			setupData((StationMeasurements) savedInstanceState.getParcelable(STATE_MEASUREMENTS));

		} else {
			showSpinner();
			compositeSubscription.add(odsManager.getMeasurements(station)
					.compose(networkUtils.<StationMeasurements>getDefaultTransformer())
					.subscribe(new Action1<StationMeasurements>() {
						@Override
						public void call(StationMeasurements stationMeasurements) {
							hideSpinner();
							setupData(stationMeasurements);
						}
					}, new ErrorActionBuilder()
							.add(new DefaultErrorAction(this, this, "failed to download measurements"))
							.add(new HideSpinnerAction(this))
							.build()));
		}

	}


	private void setupData(StationMeasurements measurements) {
		this.measurements = measurements;
		stationInfoUtils.setupStationCards(measurements, levelsCard, charValuesCard, metadataCard, mapCard);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(STATE_MEASUREMENTS, measurements);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_station_info, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.add_alarm:
				Intent addAlarmIntent = new StationSelection(station.getBodyOfWater(), station)
						.toIntent(this, NewAlarmActivity.class);
				startActivity(addAlarmIntent);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
