package de.bitdroid.flooding.utils;


import android.util.Log;

import com.crashlytics.android.Crashlytics;

import de.bitdroid.utils.Assert;
import timber.log.Timber;

public class ProductionLogTree extends Timber.HollowTree {

	private final String logPrefix;


	public ProductionLogTree(String logPrefix) {
		Assert.assertNotNull(logPrefix);
		this.logPrefix = logPrefix;
	}


	@Override
	public void d(String message, Object ... args) {
		// we are in production, remember?
	}


	@Override
	public void d(Throwable t, String message, Object ... args) {
		Log.d(logPrefix, message, t);
	}


	@Override
	public void i(String message, Object ... args) {
		Log.i(logPrefix, message);
	}


	@Override
	public void i(Throwable t, String message, Object ... args) {
		Log.i(logPrefix, message, t);
	}


	@Override
	public void w(String message, Object ... args) {
		Crashlytics.log(message);
	}


	@Override
	public void w(Throwable t, String message, Object ... args) {
		Crashlytics.log(message);
		Crashlytics.logException(t);
	}


	@Override
	public void e(String message, Object ... args) {
		Crashlytics.log(message);
	}


	@Override
	public void e(Throwable t, String message, Object ... args) {
		Crashlytics.log(message);
		Crashlytics.logException(t);
	}


}
