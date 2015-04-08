package de.bitdroid.flooding.main;


import android.app.Application;

import com.crashlytics.android.Crashlytics;

import de.bitdroid.flooding.BuildConfig;
import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.ProductionLogTree;
import timber.log.Timber;

public final class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		} else {
			Crashlytics.start(this);
			Timber.plant(new ProductionLogTree(getString(R.string.app_name)));
		}

	}

}
