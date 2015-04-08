package de.bitdroid.flooding.map;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ContextThemeWrapper;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.utils.BaseActivity;
import de.bitdroid.utils.StringUtils;

public class InfoMapActivity extends BaseActivity implements Extras {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);

		String waterName = getIntent().getStringExtra(EXTRA_WATER_NAME);
		String stationName = getIntent().getStringExtra(EXTRA_STATION_NAME);

		// set title
		if (waterName == null && stationName == null) getActionBar().setTitle(R.string.data_station_all);
		else if (stationName == null) getActionBar().setTitle(StringUtils.toProperCase(waterName));
		else {
			getActionBar().setTitle(StringUtils.toProperCase(stationName));
			getActionBar().setSubtitle(StringUtils.toProperCase(waterName));
		}

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, InfoMapFragment.newInstance(waterName, stationName))
				.commit();

	}


	public static final class InfoMapFragment extends BaseMapFragment {

		public static InfoMapFragment newInstance(
				String waterName,
				String stationName) {

			InfoMapFragment fragment = new InfoMapFragment();
			setArguments(fragment, waterName, stationName);
			return fragment;
		}

		@Override
		public void onStationClicked(Station station) {
			new AlertDialog.Builder(new ContextThemeWrapper(
					getActivity(), android.R.style.Theme_Holo_Dialog))
					.setTitle(StringUtils.toProperCase(station.getName()))
					.setMessage(getString(
							R.string.map_dialog_station_info,
							station.getLat(),
							station.getLon()))
					.setPositiveButton(R.string.btn_ok, null)
					.show();

		}

	}

}
