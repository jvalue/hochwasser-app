package de.bitdroid.flooding.app;


import com.crashlytics.android.Crashlytics;
import com.orm.SugarApp;

import de.bitdroid.flooding.BuildConfig;
import de.bitdroid.flooding.ceps.CepsModule;
import de.bitdroid.flooding.ods.OdsModule;
import io.fabric.sdk.android.Fabric;
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
				new AppModule(),
				new OdsModule(),
				new CepsModule());

		if (BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		} else {
			Fabric.with(this, new Crashlytics());
			Timber.plant(new CrashReportingTree());
		}

	}


	private static final class CrashReportingTree extends Timber.Tree {

		@Override
		public void e(String msg, Object... args) {
			Crashlytics.log(msg);
		}

		@Override
		public void e(Throwable e, String msg, Object... args) {
			Crashlytics.log(msg);
			Crashlytics.logException(e);
		}

		@Override
		public void w(String msg, Object... args) {
			Crashlytics.log(msg);
		}

		@Override
		public void w(Throwable e, String msg, Object... args) {
			Crashlytics.log(msg);
			Crashlytics.logException(e);
		}


		@Override
		protected void log(int priority, String tag, String message, Throwable t) {
			// nothing to do here
		}

	}


}
