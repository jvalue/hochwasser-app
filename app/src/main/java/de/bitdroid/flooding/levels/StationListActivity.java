package de.bitdroid.flooding.levels;

import android.content.Intent;
import android.os.Bundle;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.BaseStationSelectionFragment;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.utils.BaseActivity;


public class StationListActivity extends BaseActivity implements Extras {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);

		String waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		if (waterName != null) {
			// show regular station list
			getActionBar().setTitle(R.string.alarms_new_title_station);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.frame, StationSelectionFragment.newInstance(waterName))
					.commit();

		} else {
			// show map
			// TODO
		}

    }


	private void showMapFragment(String waterName) {
		// TODO
	}


	private void showStationInfo(String waterName, String stationName) {
		Intent intent = new Intent(getApplicationContext(), StationActivity.class);
		intent.putExtra(StationActivity.EXTRA_WATER_NAME, waterName);
		intent.putExtra(StationActivity.EXTRA_STATION_NAME, stationName);
		startActivity(intent);
		showSlideTransaction();
	}


	private void showWaterInfo(String waterName) {
		Intent intent = new Intent(getApplicationContext(), RiverGraphActivity.class);
		intent.putExtra(StationActivity.EXTRA_WATER_NAME, waterName);
		startActivity(intent);
		showSlideTransaction();
	}


	private void showSlideTransaction() {
		overridePendingTransition(R.anim.slide_enter_from_right, R.anim.slide_exit_to_left);
	}



	public static final class StationSelectionFragment extends BaseStationSelectionFragment {

		public static StationSelectionFragment newInstance(String waterName) {
			StationSelectionFragment fragment = new StationSelectionFragment();
			addArguments(fragment, waterName, true);
			return fragment;
		}

		@Override
		protected void onStationClicked(String waterName, String stationName) {
			((StationListActivity) getActivity()).showStationInfo(waterName, stationName);
		}

		@Override
		protected void onWaterClicked(String waterName) {
			((StationListActivity) getActivity()).showWaterInfo(waterName);
		}

		@Override
		protected void onMapClicked(String waterName) {
			((StationListActivity) getActivity()).showMapFragment(waterName);
		}

	}

}
