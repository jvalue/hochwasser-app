package de.bitdroid.ods.cep;

import android.content.Intent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import de.bitdroid.ods.gcm.BaseGcmIntentService;
import de.bitdroid.utils.Log;
import de.bitdroid.utils.RestCall;


public final class GcmIntentService extends BaseGcmIntentService {

	private static final String ACTION_GCM_FINISH = "de.bitdroid.ods.cep.ACTION_GCM_FINISH";

	private static final ObjectMapper mapper = new ObjectMapper();

	static final String
		EXTRA_RULE_JSON = "EXTRA_RULE_JSON";

	private static final String 
		PATH_UNREGISTER = "cep/unregister";

	private static final String
		PARAM_GCM_CLIENT_ID = "deviceId",
		PARAM_CEPS_CLIENT_ID = "clientId";


	@Override
	protected String handleRegistration(
			Intent intent, 
			String gcmClientId, 
			String cepsClientId,
			boolean register) throws Exception {

		Rule rule = null;
		try {
			rule = mapper.treeToValue(mapper.readTree(intent.getStringExtra(EXTRA_RULE_JSON)), Rule.class);
		} catch (Exception e) {
			Log.error("failed to read rule", e);
			return null;
		}

		RestCall.Builder builder = new RestCall.Builder(
				RestCall.RequestType.POST,
				CepManagerFactory.createCepManager(getApplicationContext()).getCepServerName());

		if (register) {
			builder.path(rule.getCepsRulePath()).parameter(PARAM_GCM_CLIENT_ID, gcmClientId);
			for (Map.Entry<String, String> params : rule.getParams().entrySet()) {
				builder.parameter(params.getKey(), params.getValue());
			}
			String jsonString = builder.build().execute();

			JsonNode json = mapper.readTree(jsonString);
			return json.get(PARAM_CEPS_CLIENT_ID).asText();

		} else {
			builder
				.path(PATH_UNREGISTER)
				.parameter(PARAM_CEPS_CLIENT_ID, cepsClientId)
				.build().execute();
			return cepsClientId;
		}
	}


	@Override
	protected void prepareResultIntent(Intent originalIntent, Intent resultIntent) {
		String ruleJson = originalIntent.getStringExtra(EXTRA_RULE_JSON);
		resultIntent.putExtra(EXTRA_RULE_JSON, ruleJson);
	}


	@Override
	protected String getActionName() {
		return ACTION_GCM_FINISH;
	}

}
