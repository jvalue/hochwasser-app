package de.bitdroid.flooding.app;


import android.app.Application;

import de.bitdroid.flooding.BuildConfig;
import de.bitdroid.flooding.ceps.CepsModule;
import roboguice.RoboGuice;
import timber.log.Timber;

public final class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		RoboGuice.getOrCreateBaseApplicationInjector(
				this,
				RoboGuice.DEFAULT_STAGE,
				RoboGuice.newDefaultRoboModule(this),
				new CepsModule());

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		} else {
			throw new RuntimeException("configure crashlytics and logging");
		}

	}

}
