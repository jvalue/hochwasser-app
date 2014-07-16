package de.bitdroid.flooding.ods.gcm;

import de.bitdroid.flooding.ods.data.OdsSource;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


public abstract class AbstractGcmRegistrationReceiver extends BroadcastReceiver {

	@Override
	public final void onReceive(Context context, Intent intent) {
		String source = intent.getStringExtra(GcmIntentService.EXTRA_SOURCE);
		boolean register = intent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false);
		String errorMsg = intent.getStringExtra(GcmIntentService.EXTRA_ERROR_MSG);

		onReceive(context, OdsSource.fromString(source), register, errorMsg);
	}


	protected abstract void onReceive(
			Context context, 
			OdsSource source, 
			boolean register, 
			String errorMsg);


	private static final IntentFilter intentFilter 
		= new IntentFilter(GcmIntentService.ACTION_GCM_FINISH);

	public static IntentFilter getIntentFilter() {
		return intentFilter;
	}

}
