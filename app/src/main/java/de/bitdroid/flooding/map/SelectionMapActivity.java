package de.bitdroid.flooding.map;

import android.content.Intent;

import de.bitdroid.utils.Log;

public class SelectionMapActivity extends BaseMapActivity {

	public static final String EXTRA_ACTIVITY_CLASS_NAME = "EXTRA_ACTIVITY_CLASS_NAME";

	@Override
	protected StationClickListener getStationClickListener() {
		final String className = getIntent().getStringExtra(EXTRA_ACTIVITY_CLASS_NAME);
		return new StationClickListener() {
			@Override
			public void onStationClick(Station station) {
				try {
					Class<?> clazz = Class.forName(className);
					Intent intent = new Intent(getApplicationContext(), clazz);
					intent.putExtra(EXTRA_STATION_NAME, station.getName());
					intent.putExtra(EXTRA_WATER_NAME, station.getRiver());
					startActivity(intent);

				} catch (ClassNotFoundException cnfe) {
					Log.error("failed to start target activity", cnfe);
				}
			}
		};
	}

}
