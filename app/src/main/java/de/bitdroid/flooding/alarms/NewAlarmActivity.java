package de.bitdroid.flooding.alarms;

import android.os.Bundle;
import android.widget.Toast;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.dataselection.RiverSelectionFragment;
import de.bitdroid.flooding.dataselection.StationSelectionFragment;
import de.bitdroid.flooding.utils.BaseActivity;
import de.bitdroid.utils.StringUtils;


public class NewAlarmActivity extends BaseActivity implements Extras {

	private String waterName, stationName;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			waterName = extras.getString(EXTRA_WATER_NAME);
			stationName = extras.getString(EXTRA_STATION_NAME);
		}

		// select river
		if (waterName == null && stationName == null) {
			getActionBar().setTitle(StringUtils.toProperCase(getString(
							R.string.alarms_new_title_river)));
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, RiverSelectionFragment.newInstance(
							NewAlarmActivity.class,
							NewAlarmActivity.class,
							android.R.anim.fade_in,
							android.R.anim.fade_out))
				.commit();

		// select station
		} else if (stationName == null) {
			getActionBar().setTitle(StringUtils.toProperCase(getString(
							R.string.alarms_new_title_station)));
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, StationSelectionFragment.newInstance(
							NewAlarmActivity.class,
							android.R.anim.fade_in,
							android.R.anim.fade_out,
							waterName,
							NewAlarmActivity.class,
							false))
				.commit();

		// select level
		} else {
			getActionBar().setTitle(StringUtils.toProperCase(getString(
							R.string.alarms_new_title_level)));
			getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.frame, SelectLevelFragment.newInstance(
							waterName,
							stationName))
				.commit();
		}

    }


	@Override
	protected void showExitAnimation() {
		// if last screen, show 'abort' message
		if (waterName == null && stationName == null) {
			Toast.makeText(this, getString(R.string.alarms_new_not_created), Toast.LENGTH_SHORT).show();
		} else {
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
	}

}
