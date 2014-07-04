package de.bitdroid.flooding.levels;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.StringUtils;

public class StationListActivity extends FragmentActivity {
	
	public static final String EXTRA_WATER_NAME = "waterName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.simple_fragment_container);
		String waterName = getIntent().getExtras().getString(EXTRA_WATER_NAME);

		// level text view
		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.frame, StationSelectionFragment.newInstance(waterName))
			.commit();

		// enable action bar back button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowHomeEnabled(false);
		getActionBar().setTitle(StringUtils.toProperCase(waterName));

    }


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_enter_from_left, R.anim.slide_exit_to_right);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) {
			case android.R.id.home:
				finish();
				overridePendingTransition(R.anim.slide_enter_from_left, R.anim.slide_exit_to_right);
				return true;
		}
		return super.onOptionsItemSelected(menuItem);
	}

}
