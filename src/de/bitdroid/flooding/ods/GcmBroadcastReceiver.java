package de.bitdroid.flooding.ods;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.bitdroid.flooding.utils.Log;


public final class GcmBroadcastReceiver extends BroadcastReceiver {

	private final static String 
		DATA_KEY_SOURCE = "source",
		DATA_KEY_DEBUG = "debug";


	@Override
	public void onReceive(Context context, Intent intent) {

		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = null;
		try {
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
			wl.acquire();

			String messageType = GoogleCloudMessaging.getInstance(context).getMessageType(intent);

			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				String debug = intent.getStringExtra(DATA_KEY_DEBUG);
				if (debug != null && !debug.equals("") && Boolean.valueOf(debug)) {
					handleDebug(context, intent);
				}
			}

		} finally {
			wl.release();
		}
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
