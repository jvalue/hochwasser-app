package de.bitdroid.flooding.ods;

import static de.bitdroid.flooding.ods.OdsSource.ACCOUNT;
import static de.bitdroid.flooding.ods.OdsSource.AUTHORITY;
import static de.bitdroid.flooding.ods.OdsSource.COLUMN_SERVER_ID;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import de.bitdroid.flooding.utils.Log;


final class SyncUtils {

	private SyncUtils () { }


	private static final String
			KEY_ACCOUNT_ADDED = "accountAdded",
			KEY_PERIODIC_SYNC_ADDED = "periodicSyncAdded";

	private static final String
			PREFS_NAME = "de.bitdroid.flooding.utils.SyncUtils";


	static boolean isPeriodicSyncScheduled(Context context) {
		return getSharedPreferences(context).getBoolean(KEY_PERIODIC_SYNC_ADDED, false);
	}


	static void startPeriodicSync(Context context, long pollFrequency) {
		ContentResolver.setIsSyncable(ACCOUNT, AUTHORITY, 1);
		ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, true);
		ContentResolver.addPeriodicSync(
				ACCOUNT,
				AUTHORITY,
				new Bundle(),
				pollFrequency);

		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		editor.putBoolean(KEY_PERIODIC_SYNC_ADDED,  true);
		editor.commit();
	}


	static void stopPeriodicSync(Context context) {
		ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, false);
		ContentResolver.removePeriodicSync(ACCOUNT, AUTHORITY, new Bundle());

		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		editor.putBoolean(KEY_PERIODIC_SYNC_ADDED, false);
		editor.commit();
	}


	static void startManualSync(Context context, OdsSource  source) {
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


	static void addAccount(Context context) {
		AccountManager accountManager 
			= (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		if (!accountManager.addAccountExplicitly(ACCOUNT, null, null)) {
			Log.warning("Adding account failed");
		}

		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		editor.putBoolean(KEY_ACCOUNT_ADDED,  true);
		editor.commit();
	}


	static boolean isAccountAdded(Context context) {
		SharedPreferences prefs = getSharedPreferences(context);
		return prefs.getBoolean(KEY_ACCOUNT_ADDED, false);
	}


	
	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

}
