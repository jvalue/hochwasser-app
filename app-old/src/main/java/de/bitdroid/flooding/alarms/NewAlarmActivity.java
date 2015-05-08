package de.bitdroid.flooding.alarms;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
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

		// if river and station present --> show level screen
		if (getIntent() != null && getIntent().getExtras() != null) {
			String riverName = getIntent().getExtras().getString(EXTRA_WATER_NAME);
			String stationName = getIntent().getExtras().getString(EXTRA_STATION_NAME);
			if (riverName != null && stationName != null) {
				showFragment(SelectLevelFragment.newInstance(riverName, stationName), false);
				return;
			}
		}

		// check for orientation change
		if (savedInstanceState != null) {
			showFragment(
					getFragmentManager().getFragment(savedInstanceState, STATE_FRAGMENT),
					false);
		} else {
			showFragment(new RiverSelectionFragment(), false);
		}

    }


	private void showFragment(Fragment fragment, boolean addToBackStack) {
		FragmentTransaction transaction =  getFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, fragment);
		if (addToBackStack) transaction.addToBackStack(null);
		transaction.commit();
	}


	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		Fragment currentFragment = getFragmentManager().findFragmentById(R.id.frame);
		getFragmentManager().putFragment(state, STATE_FRAGMENT, currentFragment);
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
			((NewAlarmActivity) getActivity())
					.showFragment(StationSelectionFragment.newInstance(river.getRiverName()), true);
		}

		@Override
		protected void onMapClicked() {
			((NewAlarmActivity) getActivity()).
					showFragment(MapFragment.newInstance(null), true);
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
			((NewAlarmActivity) getActivity()).
					showFragment(SelectLevelFragment.newInstance(waterName, stationName), true);
		}

		@Override
		protected void onWaterClicked(String waterName) { }

		@Override
		protected void onMapClicked(String waterName) {
			((NewAlarmActivity) getActivity()).
					showFragment(MapFragment.newInstance(waterName), true);
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
			((NewAlarmActivity) getActivity()).
					showFragment(SelectLevelFragment.newInstance(
							station.getRiver(),
							station.getName()), true);
		}

	}

}
