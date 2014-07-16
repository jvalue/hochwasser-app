package de.bitdroid.flooding.ods.data;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public final class SyncStatusListener extends BroadcastReceiver {

	private static final Object LOCK = new Object();

	private static final String PREFS_NAME = "de.bitdroid.flooding.ods.SyncStatusListener";
	private static final long PREFS_DEFAULT_TIMESTAMP = -1;

	@Override
	public void onReceive(Context context, Intent intent) {
		String sourceString = intent.getStringExtra(SyncAdapter.EXTRA_SOURCE_NAME);
		OdsSource source = OdsSource.fromString(sourceString);
		boolean success = intent.getBooleanExtra(SyncAdapter.EXTRA_SYNC_SUCCESSFUL, false);

		synchronized(LOCK) {
			SharedPreferences.Editor editor = getSharedPreferences(context).edit();
			editor.putLong(toKey(source, success), System.currentTimeMillis());
			editor.commit();
		}
	}


	static Calendar getLastSuccessfulSync(Context context, OdsSource source) {
		return getTimestamp(context, source, true);
	}


	static Calendar getLastFailedSync(Context context, OdsSource source) {
		return getTimestamp(context, source, false);
	}


	static Calendar getLastSync(Context context, OdsSource source) {
		Calendar lastSuccess, lastFailure;
		synchronized(LOCK) {
			lastSuccess = getLastSuccessfulSync(context, source);
			lastFailure = getLastFailedSync(context, source);
		}

		if (lastSuccess == null && lastFailure == null) return null;
		if (lastSuccess == null) return lastFailure;
		if (lastFailure == null) return lastSuccess;
		if (lastSuccess.after(lastFailure)) return lastSuccess;
		return lastFailure;
	}



	private static Calendar getTimestamp(Context context, OdsSource source, boolean success) {
		synchronized(LOCK) {
			SharedPreferences prefs = getSharedPreferences(context);
			long timestamp = prefs.getLong(toKey(source, success), PREFS_DEFAULT_TIMESTAMP);
			if (timestamp == PREFS_DEFAULT_TIMESTAMP) return null;

			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(timestamp);
			return c;
		}
	}


	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}


	private static final String 
		KEY_SUCCESS = "_SUCCESS",
		KEY_FALIURE = "_FAILURE";

	private static String toKey(OdsSource source, boolean success) {
		if (success) return source.toString() + KEY_SUCCESS;
		else return source.toString() + KEY_FALIURE;
	}

}
