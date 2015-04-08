package de.bitdroid.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public abstract class BaseEventReceiver extends BroadcastReceiver {

	protected final static String ACTION_EVENT_RECEIVED = "de.bitdroid.ods.cep.ACTION_EVENT_RECEIVED";
	protected final static String 
		EXTRA_EVENT_ID = "EXTRA_EVENT_ID",
		EXTRA_RULE = "EXTRA_RULE";


	@Override
	public final void onReceive(Context context, Intent intent) {
		Rule rule = intent.getParcelableExtra(EXTRA_RULE);
		String eventId = intent.getStringExtra(EXTRA_EVENT_ID);
		onReceive(context, rule, eventId);
	}


	protected abstract void onReceive(Context context, Rule rule, String eventId);

}
