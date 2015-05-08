package de.bitdroid.flooding.gcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.Set;

import timber.log.Timber;


public abstract class BaseGcmReceiver extends BroadcastReceiver {

	private static final String DATA_KEY_PING = "ping";

	@Override
	public final void onReceive(Context context, Intent intent) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = null;
		try {
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
			wl.acquire();

			String messageType = GoogleCloudMessaging.getInstance(context).getMessageType(intent);

			if (!GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) return;

			// check for server ping
			String pingExtra = intent.getExtras().getString(DATA_KEY_PING);
			if (pingExtra != null && Boolean.valueOf(pingExtra)) {
				Timber.d("found server ping, ignoring");
				return;
			}

			// forward msg to correct subclass, assuming that all subclasses
			// except disjunkt message parameters
			Bundle extras = intent.getExtras();
			for (String key : getRequiredExtras()) {
				if (!extras.containsKey(key)) return;
			}

			handle(context, intent);


		} finally {
			if (wl != null) wl.release();
		}
	}


	protected abstract Set<String> getRequiredExtras();
	protected abstract void handle(Context context, Intent intent);

}
