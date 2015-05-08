package de.bitdroid.flooding.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

import de.bitdroid.utils.Assert;


public class SyncStatusListener extends BroadcastReceiver {

	private static final Object LOCK = new Object();

	private static final String PREFS_NAME = SyncStatusListener.class.getName();
	private static final long PREFS_DEFAULT_TIMESTAMP = -1;
	private static final String KEY_SYNC_RUNNING = "SYNC_RUNNING";


	private Context context;

	/**
	 * Empty constructor for android to listen to sync status changes. Do NOT manually
	 * create and use this class, as it is lacking a context!
	 */
	public SyncStatusListener() { }


	public SyncStatusListener(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
	}


	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		String action = intent.getAction();

		if (action.equals(SyncAdapter.ACTION_SYNC_START)) {
			// nothing to do for now
			// TODO offer some method for querying sync status of single source?

		} else if (action.equals(SyncAdapter.ACTION_SYNC_FINISH)) {
			// store latest sync date
			String sourceString = intent.getStringExtra(SyncAdapter.EXTRA_SOURCE_NAME);
			OdsSource source = OdsSource.fromString(sourceString);
			boolean success = intent.getBooleanExtra(SyncAdapter.EXTRA_SYNC_SUCCESSFUL, false);

			synchronized(LOCK) {
				SharedPreferences.Editor editor = getSharedPreferences().edit();
				editor.putLong(toTimestampKey(source, success), System.currentTimeMillis());
				editor.commit();
			}

		} else if (action.equals(SyncAdapter.ACTION_SYNC_ALL_START)) {
			//  mark sync started
			SharedPreferences.Editor editor = getSharedPreferences().edit();
			editor.putBoolean(KEY_SYNC_RUNNING, true);
			editor.commit();

		} else if (action.equals(SyncAdapter.ACTION_SYNC_ALL_FINISH)) {
			// mark sync stopped
			SharedPreferences.Editor editor = getSharedPreferences().edit();
			editor.putBoolean(KEY_SYNC_RUNNING, false);
			editor.commit();
		}

	}


	Calendar getLastSuccessfulSync(OdsSource source) {
		return getTimestamp(source, true);
	}


	Calendar getLastFailedSync(OdsSource source) {
		return getTimestamp(source, false);
	}


	boolean isSyncRunning() {
		return getSharedPreferences().getBoolean(KEY_SYNC_RUNNING, false);
	}


	Calendar getLastSync(OdsSource source) {
		Calendar lastSuccess, lastFailure;
		synchronized(LOCK) {
			lastSuccess = getLastSuccessfulSync(source);
			lastFailure = getLastFailedSync(source);
		}

		if (lastSuccess == null && lastFailure == null) return null;
		if (lastSuccess == null) return lastFailure;
		if (lastFailure == null) return lastSuccess;
		if (lastSuccess.after(lastFailure)) return lastSuccess;
		return lastFailure;
	}


	private Calendar getTimestamp(OdsSource source, boolean success) {
		synchronized(LOCK) {
			SharedPreferences prefs = getSharedPreferences();
			long timestamp = prefs.getLong(toTimestampKey(source, success), PREFS_DEFAULT_TIMESTAMP);
			if (timestamp == PREFS_DEFAULT_TIMESTAMP) return null;

			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(timestamp);
			return c;
		}
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}


	private static final String 
		KEY_SUCCESS = "_SUCCESS",
		KEY_FALIURE = "_FAILURE";

	private String toTimestampKey(OdsSource source, boolean success) {
		if (success) return source.toString() + KEY_SUCCESS;
		else return source.toString() + KEY_FALIURE;
	}

}
