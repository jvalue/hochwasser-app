package de.bitdroid.flooding.app;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import javax.inject.Inject;

import de.bitdroid.flooding.R;

public class AppModule implements Module {


	@Override
	public void configure(Binder binder) {
		// nothing to do for now
	}


	@Provides
	@Inject
	public Tracker provideTracker(Context context) {
		// setup Google analytics
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
		analytics.setLocalDispatchPeriod(1);
		analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
		Tracker tracker = analytics.newTracker(context.getString(R.string.google_analytics_property_id));
		tracker.enableExceptionReporting(true);
		tracker.enableAdvertisingIdCollection(true);
		tracker.enableAutoActivityTracking(true);
		return tracker;
	}

}
