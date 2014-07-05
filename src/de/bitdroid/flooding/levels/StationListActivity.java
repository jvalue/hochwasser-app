package de.bitdroid.flooding.levels;

import android.os.Bundle;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.StringUtils;

public class StationListActivity extends BaseActivity {
	
	public static final String EXTRA_WATER_NAME = "waterName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);
		String waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame, StationSelectionFragment.newInstance(waterName))
			.commit();

		getActionBar().setTitle(StringUtils.toProperCase(waterName));

    }

}
