package de.bitdroid.flooding.map;

import android.app.AlertDialog;
import android.view.ContextThemeWrapper;

import de.bitdroid.flooding.R;
import de.bitdroid.utils.StringUtils;

public class InfoMapActivity extends BaseMapActivity {

	@Override
	protected StationClickListener getStationClickListener() {
		return new StationClickListener() {
			@Override
			public void onStationClick(Station station) {
				new AlertDialog.Builder(new ContextThemeWrapper(
						InfoMapActivity.this, android.R.style.Theme_Holo_Dialog))
						.setTitle(StringUtils.toProperCase(station.getName()))
						.setMessage(getString(
								R.string.map_dialog_station_info,
								station.getLat(),
								station.getLon()))
						.setPositiveButton(R.string.btn_ok, null)
						.show();

			}
		};
	}

}
