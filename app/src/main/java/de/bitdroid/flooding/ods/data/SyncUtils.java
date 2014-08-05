package de.bitdroid.flooding.ods.data;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import de.bitdroid.flooding.utils.Assert;
import de.bitdroid.flooding.utils.Log;

import static de.bitdroid.flooding.ods.data.OdsSource.ACCOUNT;
import static de.bitdroid.flooding.ods.data.OdsSource.AUTHORITY;
import static de.bitdroid.flooding.ods.data.OdsSource.COLUMN_SERVER_ID;


public class SyncUtils {

	private static final String
			KEY_ACCOUNT_ADDED = "accountAdded",
			KEY_PERIODIC_SYNC_ADDED = "periodicSyncAdded";

	private static final String PREFS_NAME = SyncUtils.class.getName();


	private final Context context;

	public SyncUtils(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
	}

	public boolean isPeriodicSyncScheduled() {
		return getSharedPreferences().getBoolean(KEY_PERIODIC_SYNC_ADDED, false);
	}


	public void startPeriodicSync(long pollFrequency) {
		ContentResolver.setIsSyncable(ACCOUNT, AUTHORITY, 1);
		ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, true);
		ContentResolver.addPeriodicSync(
				ACCOUNT,
				AUTHORITY,
				new Bundle(),
				pollFrequency);

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean(KEY_PERIODIC_SYNC_ADDED, true);
		editor.commit();
	}


	public void stopPeriodicSync() {
		ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, false);
		ContentResolver.removePeriodicSync(ACCOUNT, AUTHORITY, new Bundle());

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean(KEY_PERIODIC_SYNC_ADDED, false);
		editor.commit();
	}


	public void startManualSync(OdsSource source) {
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(
					source.toSyncUri(),
					new String[] { COLUMN_SERVER_ID },
					null, null, null);
		} finally {
			cursor.close();
		}
	}


	public void addAccount() {
		AccountManager accountManager 
			= (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		if (!accountManager.addAccountExplicitly(ACCOUNT, null, null)) {
			Log.warning("Adding account failed");
		}

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean(KEY_ACCOUNT_ADDED,  true);
		editor.commit();
	}


	public boolean isAccountAdded() {
		SharedPreferences prefs = getSharedPreferences();
		return prefs.getBoolean(KEY_ACCOUNT_ADDED, false);
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

}
