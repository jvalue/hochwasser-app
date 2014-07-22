package de.bitdroid.flooding.alarms;

import android.content.Context;

import de.bitdroid.flooding.ods.cep.BaseEventReceiver;
import de.bitdroid.flooding.utils.Log;


public final class EventReceiver extends BaseEventReceiver {

	@Override
	protected void onReceive(Context context, String eventId) {
		Log.info("Received event " + eventId + "!!!!!!");
	}

}
