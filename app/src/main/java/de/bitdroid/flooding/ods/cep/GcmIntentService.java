package de.bitdroid.flooding.ods.cep;

import android.content.Intent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.bitdroid.flooding.ods.gcm.BaseGcmIntentService;
import de.bitdroid.flooding.ods.utils.RestCall;


public final class GcmIntentService extends BaseGcmIntentService {

	private static final String ACTION_GCM_FINISH = "de.bitdroid.flooding.ods.cep.ACTION_GCM_FINISH";

	private static final ObjectMapper mapper = new ObjectMapper();

	static final String 
		EXTRA_EPL_STMT = "EXTRA_EPL_STMT";

	private static final String 
		PATH_REGISTER = "cep/gcm/register",
		PATH_UNREGISTER = "cep/unregister";

	private static final String
		PARAM_GCM_CLIENTID = "regId",
		PARAM_EPL_STMT = "eplStmt",
		PARAM_CEPS_CLIENTID = "clientId";


	@Override
	protected String handleRegistration(
			Intent intent, 
			String gcmClientId, 
			String cepsClientId,
			boolean register) throws Exception {

		String eplStmt = intent.getStringExtra(EXTRA_EPL_STMT);

		RestCall.Builder builder = new RestCall.Builder(
				RestCall.RequestType.POST,
				new CepManager(getApplicationContext()).getCepServerName());

		if (register) {
			String jsonString = builder
				.path(PATH_REGISTER)
				.parameter(PARAM_GCM_CLIENTID, gcmClientId)
				.parameter(PARAM_EPL_STMT, eplStmt)
				.build().execute();
			JsonNode json = mapper.readTree(jsonString);
			return json.get(PARAM_CEPS_CLIENTID).asText();

		} else {
			builder
				.path(PATH_UNREGISTER)
				.parameter(PARAM_CEPS_CLIENTID, cepsClientId)
				.build().execute();
			return cepsClientId;
		}
	}


	@Override
	protected void prepareResultIntent(Intent originalIntent, Intent resultIntent) {
		String eplStmt = originalIntent.getStringExtra(EXTRA_EPL_STMT);
		resultIntent.putExtra(EXTRA_EPL_STMT, eplStmt);
	}


	@Override
	protected String getActionName() {
		return ACTION_GCM_FINISH;
	}

}
