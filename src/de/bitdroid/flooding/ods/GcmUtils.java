package de.bitdroid.flooding.ods;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.Log;


final class GcmUtils {

	private static final String 
		PATH_NOTIFICATIONS = "notifications",
		PATH_REGISTER = "register",
		PATH_UNREGISTER = "unregister";

	private static final String
		PARAM_CLIENTID = "regId",
		PARAM_SOURCE = "source";

	private static final String PREFS_NAME = "GcmUtilities";
	private static final String 
		PREFS_KEY_CLIENTID = "clientId",
		PREFS_KEY_APP_VERSION = "appVersion";



	static void registerSource(
			Context context, 
			OdsSource source, 
			GcmRegistrationListener listener) {

		new RegistrationTask(context, source, true, listener).execute();
	}


	static void unregisterSource(
			Context context, 
			OdsSource source,
			GcmRegistrationListener listener) {

		new RegistrationTask(context, source, false, listener).execute();
	}


	private static void sourceRegistrationHelper(
			Context context,
			OdsSource source,
			boolean register) throws GcmException {

		if (context == null || source == null)
			throw new NullPointerException("params cannot be null");

		try {
			// register with google
			String clientId = getClientId(context);
			if (clientId == null) {
				clientId = GoogleCloudMessaging
						.getInstance(context)
						.register(getSenderId(context));
				SharedPreferences.Editor editor = getSharedPreferences(context).edit();
				editor.putString(PREFS_KEY_CLIENTID, clientId);
				editor.putInt(PREFS_KEY_APP_VERSION, getAppVersion(context));
				editor.commit();
			}

			// register on ods server
			RestCall.Builder builder = new RestCall.Builder(
					RestCall.RequestType.POST,
					OdsSourceManager.getInstance(context).getOdsServerName())
				.parameter(PARAM_CLIENTID, clientId)
				.parameter(PARAM_SOURCE, source.getSourceId())
				.path(PATH_NOTIFICATIONS);

			if (register) builder.path(PATH_REGISTER);
			else builder.path(PATH_UNREGISTER);

			builder.build().execute();
		} catch (IOException io) {
			Log.warning(android.util.Log.getStackTraceString(io));
			throw new GcmException(io);
		} catch (RestException re) {
			Log.warning(android.util.Log.getStackTraceString(re));
			throw new GcmException(re);
		}

		SharedPreferences.Editor editor = getSharedPreferences(context).edit();
		if (register) editor.putString(source.toString(), "");
		else {
			SharedPreferences prefs = getSharedPreferences(context);
			if (prefs.getAll().size() == 2) editor.clear();
			else editor.remove(source.toString());
		}
		editor.commit();
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
	


	private GcmUtils() { }


	private static final class RegistrationTask extends AsyncTask<Void, Void, GcmException> {
		private final Context context;
		private final OdsSource source;
		private final boolean register;
		private final GcmRegistrationListener listener;

		public RegistrationTask(
				Context context, 
				OdsSource source, 
				boolean register, 
				GcmRegistrationListener listener) {

			this.context = context;
			this.source = source;
			this.register = register;
			this.listener = listener;
		}

		@Override
		protected GcmException doInBackground(Void... param) {
			try {
				sourceRegistrationHelper(context, source, register);
				return null;
			} catch (GcmException ge) {
				return ge;
			}
		}
				
		@Override
		protected void onPostExecute(GcmException ge) {
			if (ge == null) listener.onSuccess();
			else listener.onFailure(ge);
		}
	}

}
