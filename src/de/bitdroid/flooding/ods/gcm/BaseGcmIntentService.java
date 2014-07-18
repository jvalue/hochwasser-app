package de.bitdroid.flooding.ods.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.bitdroid.flooding.utils.Log;


public abstract class BaseGcmIntentService extends IntentService {

	public static final String ACTION_GCM_FINISH = "de.bitdroid.flooding.ods.gcm.ACTION_GCM_FINISH";

	public static final String 
		EXTRA_REGISTER = "EXTRA_REGISTER",
		EXTRA_ERROR_MSG = "EXTRA_ERROR_MSG";


	private final GcmIdManager idManager;

	public BaseGcmIntentService() {
		super(BaseGcmIntentService.class.getSimpleName());
		this.idManager = GcmIdManager.getInstance(this);
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		boolean register = intent.getBooleanExtra(EXTRA_REGISTER, false);
		String clientId = idManager.getClientId();
		String senderId = idManager.getSenderId();

		String errorMsg = null;

		try {

			if (clientId == null) {
				clientId = GoogleCloudMessaging
					.getInstance(getApplicationContext())
					.register(senderId);
				idManager.updateClientId(clientId);
			}

			handleRegistration(intent, clientId, register);

		} catch (Exception e) {
			errorMsg = e.getMessage();
		}

		if (errorMsg != null) Log.error(errorMsg);

		Intent resultIntent = new Intent(ACTION_GCM_FINISH);
		resultIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
		resultIntent.putExtra(EXTRA_REGISTER, register);

		prepareResultIntent(intent, resultIntent);

		getApplicationContext().sendBroadcast(resultIntent);
	}


	protected abstract void handleRegistration(
			Intent intent, 
			String clinetId,
			boolean register) throws Exception;

	protected abstract void prepareResultIntent(Intent originalIntent, Intent resultIntent);

}
