package de.bitdroid.flooding.ui;


import android.os.Bundle;

import javax.inject.Inject;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.network.NetworkUtils;
import de.bitdroid.flooding.ods.OdsManager;

/**
 * Container activity for the main navigation drawer.
 */
public class MainDrawerActivity extends AbstractRoboDrawerActivity {

	@Inject private OdsManager odsManager;
	@Inject private NetworkUtils networkUtils;

	@Override
	public void init(Bundle bundle) {
		addSection(newSection(getString(R.string.nav_analysis), new RiverSelectionFragment()));
	}

}
