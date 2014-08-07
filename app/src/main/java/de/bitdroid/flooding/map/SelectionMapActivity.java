package de.bitdroid.flooding.map;

import android.content.Intent;

import de.bitdroid.utils.Log;

public class SelectionMapActivity extends BaseMapActivity {

	public static final String EXTRA_ACTIVITY_CLASS_NAME = "EXTRA_ACTIVITY_CLASS_NAME";
	public static final String
			EXTRA_ANIM_ENTER = "EXTRA_ANIM_ENTER",
			EXTRA_ANIM_EXIT = "EXTRA_ANIM_EXIT";


	@Override
	protected StationClickListener getStationClickListener() {
		final String className = getIntent().getStringExtra(EXTRA_ACTIVITY_CLASS_NAME);
		final int enterAnim = getIntent().getIntExtra(EXTRA_ANIM_ENTER, -1);
		final int exitAnim = getIntent().getIntExtra(EXTRA_ANIM_EXIT, -1);

		return new StationClickListener() {
			@Override
			public void onStationClick(Station station) {
				try {
					Class<?> clazz = Class.forName(className);
					Intent intent = new Intent(getApplicationContext(), clazz);
					intent.putExtra(EXTRA_STATION_NAME, station.getName());
					intent.putExtra(EXTRA_WATER_NAME, station.getRiver());
					startActivity(intent);
					overridePendingTransition(enterAnim, exitAnim);

				} catch (ClassNotFoundException cnfe) {
					Log.error("failed to start target activity", cnfe);
				}
			}
		};
	}


	@Override
	protected void showExitAnimation() {
		// TODO this is a bad hack. Solution?
		// Don't use activities, use fragments! Then there would be one activity for
		// alarm selection and one for regular data selection
		if (getIntent().getIntExtra(EXTRA_ANIM_ENTER, -1) == android.R.anim.fade_in) {
			overridePendingTransition(
					getIntent().getIntExtra(EXTRA_ANIM_ENTER, -1),
					getIntent().getIntExtra(EXTRA_ANIM_EXIT, -1));
		} else {
			super.showExitAnimation();
		}
	}

}
