package de.bitdroid.flooding.ods;

import java.io.IOException;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.bitdroid.flooding.utils.Log;


public final class GcmIntentService extends IntentService {

	public final String ACTION_GCM_FINISH = "de.bitdroid.flooding.ods.ACTION_GCM_FINISH";

	public static final String
		EXTRA_SOURCE = "EXTRA_SOURCE",
		EXTRA_ERROR_MSG = "EXTRA_ERROR_MSG",
		EXTRA_REGISTER = "EXTRA_REGISTER";

	static final String 
		EXTRA_CLIENTID = "EXTRA_CLIENTID",
		EXTRA_SENDERID = "EXTRA_SENDERID";


	private static final String 
		PATH_NOTIFICATIONS = "notifications",
		PATH_REGISTER = "register",
		PATH_UNREGISTER = "unregister";

	private static final String
		PARAM_CLIENTID = "regId",
		PARAM_SOURCE = "source";


	public GcmIntentService() {
		super(GcmIntentService.class.getSimpleName());
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		OdsSource source = OdsSource.fromString(intent.getStringExtra(EXTRA_SOURCE));
		boolean register = intent.getBooleanExtra(EXTRA_REGISTER, false);
		String clientId = intent.getStringExtra(EXTRA_CLIENTID);
		String senderId = intent.getStringExtra(EXTRA_SENDERID);

		String errorMsg = null;

		try {

			if (clientId == null)
				clientId = GoogleCloudMessaging
					.getInstance(getApplicationContext())
					.register(senderId);


			RestCall.Builder builder = new RestCall.Builder(
					RestCall.RequestType.POST,
					OdsSourceManager.getInstance(getApplicationContext()).getOdsServerName())
				.parameter(PARAM_CLIENTID, clientId)
				.parameter(PARAM_SOURCE, source.getSourceId())
				.path(PATH_NOTIFICATIONS);

			if (register) builder.path(PATH_REGISTER);
			else builder.path(PATH_UNREGISTER);

			builder.build().execute();

		} catch (IOException io) {
			errorMsg = io.getMessage();
		} catch (RestException re) {
			errorMsg = re.getMessage();
		}

		if (errorMsg != null) Log.warning(errorMsg);

		Intent finishIntent = new Intent(ACTION_GCM_FINISH);
		finishIntent.putExtra(EXTRA_SOURCE, source.toString());
		finishIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
		finishIntent.putExtra(EXTRA_CLIENTID, clientId);
		finishIntent.putExtra(EXTRA_REGISTER, register);
		getApplicationContext().sendBroadcast(finishIntent);
	}

}
