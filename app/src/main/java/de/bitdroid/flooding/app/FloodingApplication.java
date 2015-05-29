package de.bitdroid.flooding.app;


import com.orm.SugarApp;

import de.bitdroid.flooding.BuildConfig;
import de.bitdroid.flooding.ceps.CepsModule;
import de.bitdroid.flooding.ods.OdsModule;
import roboguice.RoboGuice;
import timber.log.Timber;

/**
 * Main application for flooding.
 */
public class FloodingApplication extends SugarApp {

	@Override
	public void onCreate() {
		super.onCreate();

		RoboGuice.getOrCreateBaseApplicationInjector(
				this,
				RoboGuice.DEFAULT_STAGE,
				RoboGuice.newDefaultRoboModule(this),
				new OdsModule(),
				new CepsModule());

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		} else {
			throw new RuntimeException("configure crashlytics and logging");
		}

	}

}
