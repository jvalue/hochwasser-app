package de.bitdroid.flooding.ods;

import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.utils.Log;


final class GcmUtils {

	private static final String 
		PATH_REGISTER = "register",
		PATH_UNREGISTER = "unregister";

	private static final String
		PARAM_CLIENTID = "regId",
		PARAM_SOURCE = "source";

	private static final String PREFS_NAME = "GcmUtilities";
	private static final String PREFS_KEY_CLIENTID = "clientId";



	static void registerSource(Context context, OdsSource source) throws GcmException {
		sourceRegistrationHelper(context, source, true);
	}


	static void unregisterSource(Context context, OdsSource source) throws GcmException {
		sourceRegistrationHelper(context, source, false);
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
				editor.commit();
			}

			// register on ods server
			RestCall.Builder builder = new RestCall.Builder(
					RestCall.RequestType.POST,
					OdsSourceManager.getInstance(context).getOdsServerName())
				.parameter(PARAM_CLIENTID, clientId)
				.parameter(PARAM_SOURCE, source.getSourceUrlPath());

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
		if (register) editor.putString(source.getClass().getName(), "");
		else editor.remove(source.getClass().getName());
		editor.commit();
	}


	static boolean isSourceRegistered(Context context, OdsSource source) {
		if (context == null || source == null) 
			throw new NullPointerException("params cannot be null");

		return getSharedPreferences(context).contains(source.getClass().getName());
	}


	private static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}


	private static String getSenderId(Context context) {
		return context.getString(R.string.GOOGLE_API_KEY);
	}


	private static String getClientId(Context context) {
		// TODO 
		// - Reregister source on app version change
		return getSharedPreferences(context).getString(PREFS_KEY_CLIENTID, null);
	}





	public static class GcmException extends Exception {

		public static final long serialVersionUID = 42L;

		private final String exceptionMsg;

		GcmException(Exception e) {
			super(e);
			exceptionMsg = null;
		}

		GcmException(String exceptionMsg) {
			this.exceptionMsg = exceptionMsg;
		}

		@Override
		public String getMessage() {
			if (exceptionMsg == null) return getCause().getMessage();
			else return exceptionMsg;
		}
	}

	private GcmUtils() { }
}
