package de.bitdroid.flooding.main;


import android.app.Application;

import com.crashlytics.android.Crashlytics;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.ProductionLogTree;
import de.bitdroid.utils.Debug;
import timber.log.Timber;

public final class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// only start for release builds!
		if (!Debug.isDebugBuild(this)) {
			Crashlytics.start(this);
			Timber.plant(new ProductionLogTree(getString(R.string.app_name)));
		} else {
			Timber.plant(new Timber.DebugTree());
		}

	}

}
