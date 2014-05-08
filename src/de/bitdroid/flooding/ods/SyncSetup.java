package de.bitdroid.flooding.ods;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import de.bitdroid.flooding.utils.Log;


public final class SyncSetup {

	private SyncSetup() { }


	private static final String KEY_FIRST_START = "firstStart";

	public static void setupSyncAdapter(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean firstStart = prefs.getBoolean(KEY_FIRST_START, true);

		if (firstStart) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putBoolean(KEY_FIRST_START, false);
			editor.commit();

			AccountManager accountManager 
				= (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

			if (accountManager.addAccountExplicitly(OdsContract.ACCOUNT, null, null)) {
				Log.info("Added account successfully");
			} else {
				Log.warning("Adding account failed");
			}

			// setup periodic sync
			ContentResolver.setIsSyncable(OdsContract.ACCOUNT, OdsContract.AUTHORITY, 1);
			ContentResolver.setSyncAutomatically(OdsContract.ACCOUNT, OdsContract.AUTHORITY, true);
			ContentResolver.addPeriodicSync(
					OdsContract.ACCOUNT,
					OdsContract.AUTHORITY,
					new Bundle(),
					1000 * 60 * 60);

			// trigger manual sync
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(
						OdsContract.BASE_CONTENT_URI.buildUpon().appendPath("sync").build(),
						new String[] { OdsContract.COLUMN_SERVER_ID },
						null, null, null);
			} finally {
				cursor.close();
			}
		}
	}
}
