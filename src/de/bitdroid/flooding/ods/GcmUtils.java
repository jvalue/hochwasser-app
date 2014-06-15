package de.bitdroid.flooding.ods;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import de.bitdroid.flooding.R;


final class GcmUtils extends BroadcastReceiver {

	private static final String PREFS_NAME = "GcmUtilities";
	private static final String 
		PREFS_KEY_CLIENTID = "clientId",
		PREFS_KEY_APP_VERSION = "appVersion";


	static void registerSource(
			Context context, 
			OdsSource source) {

		sourceRegistrationHelper(context, source, true);
	}


	static void unregisterSource(
			Context context, 
			OdsSource source) {

		sourceRegistrationHelper(context, source, false);
	}


	private static void sourceRegistrationHelper(
			Context context,
			OdsSource source,
			boolean register) {

		if (context == null || source == null)
			throw new NullPointerException("params cannot be null");

		Intent registrationIntent = new Intent(context, GcmIntentService.class);
		registrationIntent.putExtra(GcmIntentService.EXTRA_SOURCE, source.toString());
		registrationIntent.putExtra(GcmIntentService.EXTRA_REGISTER, register);
		registrationIntent.putExtra(GcmIntentService.EXTRA_CLIENTID, getClientId(context));
		registrationIntent.putExtra(GcmIntentService.EXTRA_SENDERID, getSenderId(context));
		context.startService(registrationIntent);
	}


	static boolean isSourceRegistered(Context context, OdsSource source) {
		if (context == null || source == null) 
			throw new NullPointerException("params cannot be null");

		return getSharedPreferences(context).contains(source.toString());
	}


	static Set<OdsSource> getRegisteredSources(Context context) {
		Set<OdsSource> sources = new HashSet<OdsSource>();
		Map<String,?> values = getSharedPreferences(context).getAll();
		for (String key : values.keySet()) {
			if (key.equals(PREFS_KEY_CLIENTID) || key.equals(PREFS_KEY_APP_VERSION)) continue;
			sources.add(OdsSource.fromString(key));
		}
		return sources;
	}



	@Override
	public void onReceive(Context context, Intent intent) {

		String sourceString = intent.getStringExtra(GcmIntentService.EXTRA_SOURCE);
		String clientId = intent.getStringExtra(GcmIntentService.EXTRA_CLIENTID);
		String errorMsg = intent.getStringExtra(GcmIntentService.EXTRA_ERROR_MSG);
		boolean register = intent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false);

		// save client id
		if (clientId != null) {
			SharedPreferences.Editor editor = getSharedPreferences(context).edit();
			editor.putString(PREFS_KEY_CLIENTID, clientId);
			editor.putInt(PREFS_KEY_APP_VERSION, getAppVersion(context));
			editor.commit();
		}

		// save registered source
		if (errorMsg != null) {
			OdsSource source = OdsSource.fromString(sourceString);
			SharedPreferences.Editor editor = getSharedPreferences(context).edit();
			if (register) editor.putString(source.toString(), "");
			else {
				SharedPreferences prefs = getSharedPreferences(context);
				if (prefs.getAll().size() == 2) editor.clear();
				else editor.remove(source.toString());
			}
			editor.commit();
		}
	}


	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}


	private static String getSenderId(Context context) {
		return context.getString(R.string.GOOGLE_API_KEY);
	}


	private static String getClientId(Context context) {
		SharedPreferences prefs = getSharedPreferences(context);

		int savedVersion = prefs.getInt(PREFS_KEY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (savedVersion < currentVersion) return null;

		return prefs.getString(PREFS_KEY_CLIENTID, null);
	}


	private static int getAppVersion(Context context) {
		try {
			PackageInfo info = context
				.getPackageManager()
				.getPackageInfo(context.getPackageName(), 0);
			return info.versionCode;
		} catch (NameNotFoundException nnfe) {
			throw new RuntimeException(nnfe);
		}
	}

}
