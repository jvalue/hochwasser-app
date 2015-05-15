package de.bitdroid.flooding.ui;


import android.content.Intent;
import android.os.Bundle;

import de.bitdroid.flooding.ods.BodyOfWater;
import de.bitdroid.flooding.ods.Station;
import timber.log.Timber;


/**
 * Launches and manages the station selection process for viewing data.
 */
public class DataActivity extends AbstractActivity {

	private static final int
			REQUEST_SELECT_RIVER = 43,
			REQUEST_SELECT_STATION = 44;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startRiverSelection();
	}


	@Override
	public void onActivityResult(int request, int status, Intent data) {
		switch(request) {
			case REQUEST_SELECT_RIVER:
				// check if aborted
				if (status != RESULT_OK) {
					finish();
					return;
				}

				BodyOfWater water = data.getParcelableExtra(RiverSelectionActivity.EXTRA_BODY_OF_WATER);
				startStationSelection(water);
				Timber.d("selected " + water.getName());
				break;

			case REQUEST_SELECT_STATION:
				// check if aborted
				if (status != RESULT_OK) {
					startRiverSelection();
					return;
				}

				Station station = data.getParcelableExtra(StationSelectionActivity.EXTRA_STATION);
				Timber.d("selected " + station.getStationName());
				finish();
				break;
		}
	}


	private void startRiverSelection() {
		Intent intent = new Intent(this, RiverSelectionActivity.class);
		startActivityForResult(intent, REQUEST_SELECT_RIVER);
	}


	private void startStationSelection(BodyOfWater water) {
		Intent intent = new Intent(this, StationSelectionActivity.class);
		intent.putExtra(StationSelectionActivity.EXTRA_BODY_OF_WATER, water);
		startActivityForResult(intent, REQUEST_SELECT_STATION);
	}

}
