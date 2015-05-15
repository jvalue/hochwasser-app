package de.bitdroid.flooding.ui;


import android.content.Intent;
import android.os.Bundle;

import de.bitdroid.flooding.ods.BodyOfWater;
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
		StationSelection selection = new StationSelection(data);

		switch(request) {
			case REQUEST_SELECT_RIVER:
				// check if aborted
				if (status != RESULT_OK) {
					finish();
					return;
				}

				startStationSelection(selection.getWater());
				Timber.d("selected " + selection.getWater().getName());

				if (selection.getStation() != null) {
					Timber.d("selected " + selection.getStation().getStationName());
					finish();
					return;
				}

				break;

			case REQUEST_SELECT_STATION:
				// check if aborted
				if (status != RESULT_OK) {
					startRiverSelection();
					return;
				}

				Timber.d("selected " + selection.getStation().getStationName());
				finish();
				break;
		}
	}


	private void startRiverSelection() {
		startActivityForResult(
				new StationSelection().toIntent(this, RiverSelectionActivity.class),
				REQUEST_SELECT_RIVER);
	}


	private void startStationSelection(BodyOfWater water) {
		startActivityForResult(
				new StationSelection(water).toIntent(this, StationSelectionActivity.class),
				REQUEST_SELECT_STATION);
	}

}
