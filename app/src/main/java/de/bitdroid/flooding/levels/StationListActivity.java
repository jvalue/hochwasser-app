package de.bitdroid.flooding.levels;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.BaseStationSelectionFragment;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.map.BaseMapFragment;
import de.bitdroid.flooding.map.Station;
import de.bitdroid.flooding.utils.BaseActivity;


public class StationListActivity extends BaseActivity implements Extras {

	private static final String STATE_FRAGMENT = "STATE_FRAGMENT";

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);

		getActionBar().setTitle(R.string.alarms_new_title_station);

		String waterName = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) waterName = extras.getString(EXTRA_WATER_NAME);

		Fragment fragment;
		if (savedInstanceState != null) {
			fragment = getSupportFragmentManager().getFragment(savedInstanceState, STATE_FRAGMENT);
		} else {
			if (waterName != null) fragment = StationSelectionFragment.newInstance(waterName);
			else fragment = MapFragment.newInstance(waterName);
		}

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, fragment)
				.commit();

    }


	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame);
		getSupportFragmentManager().putFragment(state, STATE_FRAGMENT, currentFragment);
	}


	private void showMapFragment(String waterName) {
		getSupportFragmentManager()
				.beginTransaction()
				.addToBackStack(null)
				.replace(R.id.frame, MapFragment.newInstance(waterName))
				.commit();
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


	public static final class MapFragment extends BaseMapFragment {

		public static MapFragment newInstance(String waterName) {
			MapFragment fragment = new MapFragment();
			setArguments(fragment, waterName, null);
			return fragment;
		}

		@Override
		public void onStationClicked(Station station) {
			((StationListActivity) getActivity()).showStationInfo(station.getRiver(), station.getName());
		}

	}

}
