package de.bitdroid.ods.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import de.bitdroid.utils.Log;


public abstract class BaseGcmIntentService extends IntentService {

	public static final String
		EXTRA_REGISTER = "EXTRA_REGISTER",
		EXTRA_SERVICE_CLIENTID = "EXTRA_SERVICE_CLIENTID",
		EXTRA_ERROR_MSG = "EXTRA_ERROR_MSG";


	private final GcmIdManager gcmIdManager;


	public BaseGcmIntentService() {
		super(BaseGcmIntentService.class.getSimpleName());
		this.gcmIdManager = new GcmIdManager(this);
	}


	@Override
	protected void onHandleIntent(Intent intent) {
		String gcmClientId = gcmIdManager.getClientId();
		String senderId = gcmIdManager.getSenderId();
		String serviceClientId = intent.getStringExtra(EXTRA_SERVICE_CLIENTID);
		boolean register = intent.getBooleanExtra(EXTRA_REGISTER, false);

		String errorMsg = null;

		try {

			if (gcmClientId == null) {
				gcmClientId = GoogleCloudMessaging
					.getInstance(this)
					.register(senderId);
				gcmIdManager.updateClientId(gcmClientId);
			}

			serviceClientId = handleRegistration(intent, gcmClientId, serviceClientId, register);

		} catch (Exception e) {
			errorMsg = e.getMessage();
			Log.error(errorMsg);
		}

		Intent resultIntent = new Intent(getActionName());
		resultIntent.putExtra(EXTRA_SERVICE_CLIENTID, serviceClientId);
		resultIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
		resultIntent.putExtra(EXTRA_REGISTER, register);

		prepareResultIntent(intent, resultIntent);

		sendBroadcast(resultIntent);
	}


	protected abstract String handleRegistration(
			Intent intent, 
			String gcmClientId,
			String serviceClientId,
			boolean register) throws Exception;

	protected abstract void prepareResultIntent(Intent originalIntent, Intent resultIntent);

	protected abstract String getActionName();

}
