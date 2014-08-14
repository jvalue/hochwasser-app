package de.bitdroid.flooding.alarms;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.BaseRiverSelectionFragment;
import de.bitdroid.flooding.dataselection.BaseStationSelectionFragment;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.map.BaseMapFragment;
import de.bitdroid.flooding.map.Station;
import de.bitdroid.flooding.utils.BaseActivity;


public class NewAlarmActivity extends BaseActivity implements Extras {

	private static final String STATE_FRAGMENT = "currentFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);

		Fragment fragment;
		if (savedInstanceState != null) {
			fragment = getSupportFragmentManager().getFragment(savedInstanceState, STATE_FRAGMENT);
		} else {
			fragment = new RiverSelectionFragment();
		}

		// show all rivers fragment
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, fragment)
				.commit();
    }


	private void showStationFragment(String waterName) {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, StationSelectionFragment.newInstance(waterName))
				.addToBackStack(null)
				.commit();
	}


	private void showMapFragment(String waterName) {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, MapFragment.newInstance(waterName))
				.addToBackStack(null)
				.commit();
	}


	private void showLevelFragment(String waterName, String stationName) {
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, SelectLevelFragment.newInstance(waterName, stationName))
				.addToBackStack(null)
				.commit();
	}


	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame);
		getSupportFragmentManager().putFragment(state, STATE_FRAGMENT, currentFragment);
	}


	@Override
	protected void showExitAnimation() { }



	public static final class RiverSelectionFragment extends BaseRiverSelectionFragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			getActivity().getActionBar().setTitle(R.string.alarms_new_title_river);
			return super.onCreateView(inflater, container, savedInstanceState);
		}

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
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			getActivity().getActionBar().setTitle(getString(R.string.alarms_new_title_station));
			return super.onCreateView(inflater, container, savedInstanceState);
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


	public static final class MapFragment extends BaseMapFragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			getActivity().getActionBar().setTitle(getString(R.string.alarms_new_title_station));
			return super.onCreateView(inflater, container, savedInstanceState);
		}

		public static MapFragment newInstance(String waterName) {
			MapFragment fragment = new MapFragment();
			setArguments(fragment, waterName, null);
			return fragment;
		}

		@Override
		public void onStationClicked(Station station) {
			((NewAlarmActivity) getActivity()).showLevelFragment(station.getRiver(), station.getName());
		}

	}


}
