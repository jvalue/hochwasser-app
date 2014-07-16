package de.bitdroid.flooding.ods.gcm;

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
import de.bitdroid.flooding.ods.data.OdsSource;


public final class GcmUtils extends BroadcastReceiver {

	private static final String PREFS_NAME = "GcmUtilities";
	private static final String 
		PREFS_KEY_CLIENTID = "clientId",
		PREFS_KEY_APP_VERSION = "appVersion";


	public static void registerSource(
			Context context, 
			OdsSource source) {

		sourceRegistrationHelper(context, source, true);
	}


	public static void unregisterSource(
			Context context, 
			OdsSource source) {

		sourceRegistrationHelper(context, source, false);
	}


	private static void sourceRegistrationHelper(
			Context context,
			OdsSource source,
			boolean register) {

		// mark task pending
		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		if (register) editor.putString(source.toString(), GcmStatus.PENDING_REGISTRATION.name());
		else editor.putString(source.toString(), GcmStatus.PENDING_UNREGISTRATION.name());

		// execute in background
		Intent registrationIntent = new Intent(context, GcmIntentService.class);
		registrationIntent.putExtra(GcmIntentService.EXTRA_SOURCE, source.toString());
		registrationIntent.putExtra(GcmIntentService.EXTRA_REGISTER, register);
		registrationIntent.putExtra(GcmIntentService.EXTRA_CLIENTID, getClientId(context));
		registrationIntent.putExtra(GcmIntentService.EXTRA_SENDERID, getSenderId(context));
		context.startService(registrationIntent);
	}


	public static GcmStatus getRegistrationStatus(Context context, OdsSource source) {
		String status = getSharedPreferences(context).getString(source.toString(), null);
		if (status == null) return GcmStatus.UNREGISTERED;
		else return GcmStatus.valueOf(status);
	}


	public static Set<OdsSource> getRegisteredSources(Context context) {
		Set<OdsSource> sources = new HashSet<OdsSource>();
		Map<String,?> values = getSharedPreferences(context).getAll();
		for (Map.Entry<String, ?> e : values.entrySet()) {
			if (e.getKey().equals(PREFS_KEY_CLIENTID) 
					|| e.getKey().equals(PREFS_KEY_APP_VERSION)) 
				continue;

			GcmStatus status = GcmStatus.valueOf(e.getValue().toString());
			if (status.equals(GcmStatus.REGISTERED)) sources.add(OdsSource.fromString(e.getKey()));
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

		// clear pending flag
		if (errorMsg != null) register = !register;

		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		if (register) editor.putString(sourceString, GcmStatus.REGISTERED.name());
		else editor.putString(sourceString, GcmStatus.UNREGISTERED.name());
		editor.commit();
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
