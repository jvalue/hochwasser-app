package de.bitdroid.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public abstract class BaseEventReceiver extends BroadcastReceiver {

	protected final static String ACTION_EVENT_RECEIVED = "de.bitdroid.ods.cep.ACTION_EVENT_RECEIVED";
	protected final static String 
		EXTRA_EVENT_ID = "event",
		EXTRA_RULE_JSON = "rule";


	@Override
	public final void onReceive(Context context, Intent intent) {
		String ruleJson = intent.getStringExtra(EXTRA_RULE_JSON);
		String eventId = intent.getStringExtra(EXTRA_EVENT_ID);
		onReceive(context, ruleJson, eventId);
	}


	protected abstract void onReceive(Context context, String ruleJson, String eventId);

}
