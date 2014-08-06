package de.bitdroid.flooding.ods.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import de.bitdroid.flooding.R;
import de.bitdroid.utils.Assert;


public final class GcmIdManager {

	private static final String PREFS_NAME = GcmIdManager.class.getName();
	private static final String 
		PREFS_KEY_CLIENTID = "clientId",
		PREFS_KEY_APP_VERSION = "appVersion";


	private final Context context;

	public GcmIdManager(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
	}


	public synchronized void updateClientId(String clientId) {
		Assert.assertNotNull(clientId);

		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		editor.putString(PREFS_KEY_CLIENTID, clientId);
		editor.putInt(PREFS_KEY_APP_VERSION, getAppVersion());
		editor.commit();
	}


	public synchronized String getClientId() {
		SharedPreferences prefs = getSharedPreferences(context);

		int savedVersion = prefs.getInt(PREFS_KEY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion();
		if (savedVersion < currentVersion) return null;

		return prefs.getString(PREFS_KEY_CLIENTID, null);
	}


	public synchronized String getSenderId() {
		return context.getString(R.string.GOOGLE_API_KEY);
	}


	private int getAppVersion() {
		try {
			PackageInfo info = context
				.getPackageManager()
				.getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException nnfe) {
			throw new RuntimeException(nnfe);
		}
	}


	private SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

}
