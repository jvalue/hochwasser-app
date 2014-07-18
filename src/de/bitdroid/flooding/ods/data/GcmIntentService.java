package de.bitdroid.flooding.ods.data;

import android.content.Intent;

import de.bitdroid.flooding.ods.gcm.BaseGcmIntentService;
import de.bitdroid.flooding.ods.utils.RestCall;


public final class GcmIntentService extends BaseGcmIntentService {

	static final String 
		EXTRA_SOURCE = "EXTRA_SOURCE";

	private static final String 
		PATH_NOTIFICATIONS = "notifications",
		PATH_REGISTER = "register",
		PATH_UNREGISTER = "unregister";

	private static final String
		PARAM_CLIENTID = "regId",
		PARAM_SOURCE = "source";


	@Override
	protected void handleRegistration(
			Intent intent, 
			String clientId, 
			boolean register) throws Exception {

		OdsSource source = fromIntent(intent);

		RestCall.Builder builder = new RestCall.Builder(
				RestCall.RequestType.POST,
				OdsSourceManager.getInstance(getApplicationContext()).getOdsServerName())
			.parameter(PARAM_CLIENTID, clientId)
			.parameter(PARAM_SOURCE, source.getSourceId())
			.path(PATH_NOTIFICATIONS);

		if (register) builder.path(PATH_REGISTER);
		else builder.path(PATH_UNREGISTER);

		builder.build().execute();
	}


	@Override
	protected void prepareResultIntent(Intent originalIntent, Intent resultIntent) {
		OdsSource source = fromIntent(originalIntent);
		resultIntent.putExtra(EXTRA_SOURCE, source.toString());
	}


	private static OdsSource fromIntent(Intent intent) {
		return OdsSource.fromString(intent.getStringExtra(EXTRA_SOURCE));
	}

}
