package de.bitdroid.flooding.ods.data;

import android.content.Intent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.bitdroid.flooding.ods.gcm.BaseGcmIntentService;
import de.bitdroid.utils.RestCall;


public final class GcmIntentService extends BaseGcmIntentService {

	private static final String ACTION_GCM_FINISH = "de.bitdroid.flooding.ods.data.ACTION_GCM_FINISH";

	private static final ObjectMapper mapper = new ObjectMapper();

	static final String 
		EXTRA_SOURCE = "EXTRA_SOURCE";

	private static final String 
		PATH_REGISTER = "notifications/gcm/register",
		PATH_UNREGISTER = "notifications/unregister";

	private static final String
		PARAM_GCM_CLIENTID = "gcmClientId",
		PARAM_ODS_CLIENTID = "clientId",
		PARAM_SOURCE = "source";


	@Override
	protected String handleRegistration(
			Intent intent, 
			String gcmClientId, 
			String odsClientId,
			boolean register) throws Exception {

		OdsSource source = fromIntent(intent);

		RestCall.Builder builder = new RestCall.Builder(
				RestCall.RequestType.POST,
				OdsSourceManager.getInstance(getApplicationContext()).getOdsServerName());

		if (register) {
			String jsonString = builder
				.path(PATH_REGISTER)
				.parameter(PARAM_GCM_CLIENTID, gcmClientId)
				.parameter(PARAM_SOURCE, source.getSourceId())
				.build().execute();
			JsonNode json = mapper.readTree(jsonString);
			return json.get(PARAM_ODS_CLIENTID).asText();

		} else {
			builder
				.path(PATH_UNREGISTER)
				.parameter(PARAM_ODS_CLIENTID, odsClientId)
				.build().execute();
			return odsClientId;
		}
	}


	@Override
	protected void prepareResultIntent(Intent originalIntent, Intent resultIntent) {
		OdsSource source = fromIntent(originalIntent);
		resultIntent.putExtra(EXTRA_SOURCE, source.toString());
	}


	@Override
	protected String getActionName() {
		return ACTION_GCM_FINISH;
	}


	private static OdsSource fromIntent(Intent intent) {
		return OdsSource.fromString(intent.getStringExtra(EXTRA_SOURCE));
	}

}
