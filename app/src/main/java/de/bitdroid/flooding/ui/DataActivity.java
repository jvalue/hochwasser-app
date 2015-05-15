package de.bitdroid.flooding.ui;


import android.content.Intent;
import android.os.Bundle;

import timber.log.Timber;


/**
 * Launches and manages the station selection process for viewing data.
 *
 * The selection processes is started via startActivityForResult. This class
 * wraps those calls to simulate a continuous back stack, instead of multiple
 * seperate activities.
 */
public class DataActivity extends AbstractActivity {

	private static final int
			REQUEST_SELECT_STATION = 44,
			REQUEST_SHOW_STATION = 46;

	private StationSelection stationSelection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startActivityForResult(
				new StationSelection().toIntent(this, RiverSelectionActivity.class),
				REQUEST_SELECT_STATION);
	}


	@Override
	public void onActivityResult(int request, int status, Intent data) {

		switch(request) {
			case REQUEST_SELECT_STATION:
				// check if aborted
				if (status != RESULT_OK) {
					finish();
					return;
				}

				stationSelection = new StationSelection(data);
				Timber.d("selected " + stationSelection.getStation().getStationName());
				startActivityForResult(stationSelection.toIntent(this, StationInfoActivity.class), REQUEST_SHOW_STATION);
				break;

			case REQUEST_SHOW_STATION:
				// restore station selection back stack
				startActivityForResult(stationSelection.toIntent(this, RiverSelectionActivity.class), REQUEST_SELECT_STATION);
				break;
		}
	}

}
