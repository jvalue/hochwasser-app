package de.bitdroid.flooding.levels;

import android.os.Bundle;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.RiverSelectionFragment;
import de.bitdroid.flooding.dataselection.StationSelectionFragment;
import de.bitdroid.flooding.utils.StringUtils;


public class StationListActivity extends BaseActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);
		String waterName = getIntent().getExtras().getString(RiverSelectionFragment.EXTRA_WATER_NAME);

		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame, StationSelectionFragment.newInstance(
						StationGraphActivity.class,
						R.anim.slide_enter_from_right,
						R.anim.slide_exit_to_left,
						waterName,
						RiverGraphActivity.class,
						true))
			.commit();

		getActionBar().setTitle(StringUtils.toProperCase(waterName));

    }

}
