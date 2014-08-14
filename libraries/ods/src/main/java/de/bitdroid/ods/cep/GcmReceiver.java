package de.bitdroid.ods.cep;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.bitdroid.ods.gcm.BaseGcmReceiver;
import de.bitdroid.utils.Log;


public final class GcmReceiver extends BaseGcmReceiver {

	private static final ObjectMapper mapper = new ObjectMapper();

	private final static String 
		EXTRA_CLIENT_ID = "client",
		EXTRA_EVENT_ID = "event",
		EXTRA_DEBUG = "debug";

	private static final Set<String> REQUIRED_EXTRAS;

	static {
		Set<String> extras = new HashSet<String>(Arrays.asList(EXTRA_CLIENT_ID, EXTRA_EVENT_ID));
		REQUIRED_EXTRAS = Collections.unmodifiableSet(extras);
	}


	@Override
	protected void handle(Context context, Intent intent) {
		String clientId = intent.getStringExtra(EXTRA_CLIENT_ID);
		String eventId = intent.getStringExtra(EXTRA_EVENT_ID);

		// debug output
		String debug = intent.getStringExtra(EXTRA_DEBUG);
		if (debug != null && !debug.equals("") && Boolean.valueOf(debug)) {
			handleDebug(context, intent);
		}

		// get rule for id
		CepManager manager = CepManagerFactory.createCepManager(context);
		Rule rule = manager.getRuleForClientId(clientId);
		if (rule == null) {
			Log.warning("found rule that should be registered, but wasn't");
			manager.unregisterClientId(clientId);
			return;
		}

		// broadcast new event
		Intent eventIntent = new Intent(BaseEventReceiver.ACTION_EVENT_RECEIVED);
		eventIntent.putExtra(BaseEventReceiver.EXTRA_RULE_JSON, mapper.valueToTree(rule).toString());
		eventIntent.putExtra(BaseEventReceiver.EXTRA_EVENT_ID, eventId);
		context.sendBroadcast(eventIntent);

	}


	@Override
	protected Set<String> getRequiredExtras() {
		return REQUIRED_EXTRAS;
	}


	private void handleDebug(Context context, Intent intent) {
		StringBuilder builder = new StringBuilder();
		builder.append("Push Notification received: \n\n");

		Bundle extras = intent.getExtras();
		for (String key : extras.keySet()) {
			Object value = extras.get(key);
			builder.append(key + ":\t" + value.toString() + "\n");
		}

		Log.debug(builder.toString());
	}

}
