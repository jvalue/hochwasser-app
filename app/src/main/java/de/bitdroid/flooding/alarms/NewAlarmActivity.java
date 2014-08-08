package de.bitdroid.flooding.alarms;

import android.os.Bundle;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.BaseRiverSelectionFragment;
import de.bitdroid.flooding.dataselection.BaseStationSelectionFragment;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.utils.BaseActivity;


public class NewAlarmActivity extends BaseActivity implements Extras {

	private String waterName, stationName;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);

		// show all rivers fragment
		getActionBar().setTitle(R.string.alarms_new_title_river);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, new RiverSelectionFragment())
				.commit();
    }


	private void showStationFragment(String waterName) {
		getActionBar().setTitle(getString(R.string.alarms_new_title_station));
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, StationSelectionFragment.newInstance(waterName))
				.addToBackStack(null)
				.commit();
	}


	private void showMapFragment(String waterName) {
		getActionBar().setTitle(getString(R.string.alarms_new_title_station));
		// TODO
		throw new IllegalStateException("not supported");
	}


	private void showLevelFragment(String waterName, String stationName) {
		getActionBar().setTitle(getString(R.string.alarms_new_title_level));
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, SelectLevelFragment.newInstance(waterName, stationName))
				.addToBackStack(null)
				.commit();
	}


	@Override
	protected void showExitAnimation() { }



	public static final class RiverSelectionFragment extends BaseRiverSelectionFragment {

		@Override
		protected void onItemClicked(River river) {
			((NewAlarmActivity) getActivity()).showStationFragment(river.getRiverName());
		}

		@Override
		protected void onMapClicked() {
			((NewAlarmActivity) getActivity()).showMapFragment(null);
		}

	}


	public static final class StationSelectionFragment extends BaseStationSelectionFragment {

		public static StationSelectionFragment newInstance(String waterName) {
			StationSelectionFragment fragment = new StationSelectionFragment();
			addArguments(fragment, waterName, false);
			return fragment;
		}

		@Override
		protected void onStationClicked(String waterName, String stationName) {
			((NewAlarmActivity) getActivity()).showLevelFragment(waterName, stationName);
		}

		@Override
		protected void onWaterClicked(String waterName) { }

		@Override
		protected void onMapClicked(String waterName) {
			((NewAlarmActivity) getActivity()).showMapFragment(waterName);
		}

	}


}
