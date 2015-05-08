package de.bitdroid.flooding.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.bitdroid.flooding.gcm.BaseGcmReceiver;
import timber.log.Timber;


public final class GcmReceiver extends BaseGcmReceiver {

	private final static String 
		EXTRA_SOURCE = "source",
		EXTRA_DEBUG = "debug";

	private static final Set<String> REQUIRED_EXTRAS;

	static {
		Set<String> extras = new HashSet<String>(Arrays.asList(EXTRA_SOURCE));
		REQUIRED_EXTRAS = Collections.unmodifiableSet(extras);
	}


	@Override
	protected void handle(Context context, Intent intent) {
		// sync sources
		String sourceString = intent.getStringExtra(EXTRA_SOURCE);
		if (sourceString != null) {
			OdsSourceManager manager = OdsSourceManager.getInstance(context);
			Set<OdsSource> sources = manager.getPushNotificationSources();
			for (OdsSource source : sources) {
				if (!source.getSourceId().equals(sourceString)) continue;
				manager.startManualSync(source);
			}
		}


		// debug output
		String debug = intent.getStringExtra(EXTRA_DEBUG);
		if (debug != null && !debug.equals("") && Boolean.valueOf(debug)) {
			handleDebug(context, intent);
		}
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

		Timber.d(builder.toString());
	}

}
