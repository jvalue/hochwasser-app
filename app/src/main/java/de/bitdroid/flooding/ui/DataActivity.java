package de.bitdroid.flooding.ui;


import android.content.Intent;
import android.os.Bundle;

import de.bitdroid.flooding.ods.BodyOfWater;
import timber.log.Timber;

public class DataActivity extends AbstractActivity {

	private static final int REQUEST_SELECT_RIVER = 43;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = new Intent(this, RiverSelectionActivity.class);
		startActivityForResult(intent, REQUEST_SELECT_RIVER);
	}


	@Override
	public void onActivityResult(int request, int status, Intent data) {
		switch(request) {
			case REQUEST_SELECT_RIVER:
				if (status != RESULT_OK) return;
				BodyOfWater water = data.getParcelableExtra(RiverSelectionActivity.EXTRA_BODY_OF_WATER);
				Timber.d("selected " + water.getName());
				finish();
				break;
		}
	}

}
