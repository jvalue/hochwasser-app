package de.bitdroid.flooding.ui;


import android.content.Intent;
import android.os.Bundle;

import de.bitdroid.flooding.R;

/**
 * Container activity for the main navigation drawer.
 */
public class MainDrawerActivity extends AbstractRoboDrawerActivity {

	@Override
	public void init(Bundle bundle) {
		addSection(newSection("Dummy", new DummyFragment()));
		Intent intent = new Intent(this, DataSelectionHandler.WaterSelectionActivity.class);
		addSection(newSection(getString(R.string.nav_alarms), new AlarmsFragment()));
		addSection(newSection(getString(R.string.nav_analysis), intent));
	}

}
