package de.bitdroid.flooding.levels;

import android.os.Bundle;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.dataselection.Extras;
import de.bitdroid.flooding.dataselection.StationSelectionFragment;
import de.bitdroid.utils.StringUtils;


public class StationListActivity extends BaseActivity implements Extras {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);
		String waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

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
