package de.bitdroid.flooding.ods.cep;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.bitdroid.flooding.ods.gcm.BaseGcmReceiver;
import de.bitdroid.flooding.utils.Log;


public final class GcmReceiver extends BaseGcmReceiver {

	private final static String 
		EXTRA_CLIENTID = "client",
		EXTRA_EVENTID = "event",
		EXTRA_DEBUG = "debug";

	private static final Set<String> REQUIRED_EXTRAS;

	static {
		Set<String> extras = new HashSet<String>(Arrays.asList(EXTRA_CLIENTID, EXTRA_EVENTID));
		REQUIRED_EXTRAS = Collections.unmodifiableSet(extras);
	}


	@Override
	protected void handle(Context context, Intent intent) {
		String clientId = intent.getStringExtra(EXTRA_CLIENTID);
		String eventId = intent.getStringExtra(EXTRA_EVENTID);

		// debug output
		String debug = intent.getStringExtra(EXTRA_DEBUG);
		if (debug != null && !debug.equals("") && Boolean.valueOf(debug)) {
			handleDebug(context, intent);
		}

		// get stmt for id
		CepManager manager = CepManagerFactory.createCepManager(context);
		String eplStmt = manager.getEplStmtForClientId(clientId);
		if (eplStmt == null) {
			Log.warning("found eplStmt that should be registered, but wasn't");
			manager.unregisterClientId(clientId);
			return;
		}

		// broadcast new event
		Intent eventIntent = new Intent(BaseEventReceiver.ACTION_EVENT_RECEIVED);
		eventIntent.putExtra(BaseEventReceiver.EXTRA_EPLSTMT, eplStmt);
		eventIntent.putExtra(BaseEventReceiver.EXTRA_EVENTID, eventId);
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
