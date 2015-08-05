package de.bitdroid.flooding.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
public class StationInfoActivity extends AbstractRestrictedActivity implements SwipeRefreshLayout.OnRefreshListener {

	private static final String STATE_MEASUREMENTS = "STATE_MEASUREMENTS";

	@InjectView(R.id.card_levels) private CardView levelsCard;
	@InjectView(R.id.card_char_values) private CardView charValuesCard;
	@InjectView(R.id.card_metadata) private CardView metadataCard;
	@InjectView(R.id.card_map) private CardView mapCard;
	@InjectView(R.id.refresh_layout) private SwipeRefreshLayout refreshLayout;

	@Inject private OdsManager odsManager;
	@Inject private StationInfoUtils stationInfoUtils;

	private Station station;
	private StationMeasurements measurements;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		analyticsUtils.onScreen("station details screen");

		// set title
		station = new StationSelection(getIntent()).getStation();
		getSupportActionBar().setTitle(StringUtils.toProperCase(station.getStationName()));
		getSupportActionBar().setSubtitle(StringUtils.toProperCase(station.getBodyOfWater().getName()));

		// load data
		if (savedInstanceState != null && savedInstanceState.get(STATE_MEASUREMENTS) != null) {
			displayData((StationMeasurements) savedInstanceState.getParcelable(STATE_MEASUREMENTS));

		} else {
			showSpinner();
			loadData();
		}

		// setup swipe to refresh
		refreshLayout.setOnRefreshListener(this);
		refreshLayout.setColorSchemeResources(R.color.blue_light, R.color.blue, R.color.blue_dark);
	}


	private void loadData() {
		compositeSubscription.add(odsManager.getMeasurements(station)
				.compose(networkUtils.<StationMeasurements>getDefaultTransformer())
				.subscribe(new Action1<StationMeasurements>() {
					@Override
					public void call(StationMeasurements stationMeasurements) {
						if (isSpinnerVisible()) hideSpinner();
						if (refreshLayout.isRefreshing()) refreshLayout.setRefreshing(false);
						displayData(stationMeasurements);
					}
				}, new ErrorActionBuilder()
						.add(new DefaultErrorAction(this, this, "failed to download measurements"))
						.add(new HideSpinnerAction(this))
						.build()));
	}


	private void displayData(StationMeasurements measurements) {
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
				analyticsUtils.onClick("add new alarm");
				Intent addAlarmIntent = new StationSelection(station.getBodyOfWater(), station)
						.toIntent(this, NewAlarmActivity.class);
				startActivity(addAlarmIntent);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onRefresh() {
		// ignore if still loading
		analyticsUtils.onClick("refresh data");
		if (isSpinnerVisible()) return;
		refreshLayout.setRefreshing(true);
		loadData();
	}

}
