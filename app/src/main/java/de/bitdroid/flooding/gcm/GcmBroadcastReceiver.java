package de.bitdroid.flooding.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Accepts GCM messages and starts a service for further processing.
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent(context, GcmService.class);
		serviceIntent.putExtras(intent);
		startWakefulService(context, serviceIntent);
		setResultCode(Activity.RESULT_OK);
	}

}
