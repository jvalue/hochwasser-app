package de.bitdroid.flooding.ods.cep;

import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.bitdroid.flooding.ods.gcm.GcmRegistrationManager;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.utils.Assert;


public final class GcmManager {

	public static final String ACTION_REGISTRATION_STATUS_CHANGED = "de.bitdroid.flooding.ods.cep.ACTION_REGISTRATION_STATUS_CHANGED";

	public static final String
		EXTRA_EPL_STMT = "EXTRA_EPL_STMT",
		EXTRA_REGISTER = "EXTRA_REGISTER",
		EXTRA_ERROR_MSG = "EXTRA_ERROR_MSG";



	private static GcmManager instance;

	static GcmManager getInstance(Context context) {
		Assert.assertNotNull(context);
		if (instance == null) instance = new GcmManager(context);
		return instance;
	}


	private final Context context;
	private final GcmRegistrationManager registrationManager;

	private GcmManager(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
		this.registrationManager = new GcmRegistrationManager(context, GcmManager.class.getName());
	}


	void registerEplStmt(String eplStmt) {
		sourceRegistrationHelper(eplStmt, true);
	}


	void unregisterEplStmt(String eplStmt) {
		sourceRegistrationHelper(eplStmt, false);
	}


	private void sourceRegistrationHelper(String eplStmt, boolean register) {
		String clientId =  registrationManager.getClientId(eplStmt);

		// mark task pending
		GcmStatus status = null;
		if (register) status = GcmStatus.PENDING_REGISTRATION;
		else status = GcmStatus.PENDING_UNREGISTRATION;
		registrationManager.update(eplStmt, clientId, status);

		// execute in background
		Intent registrationIntent = new Intent(context, GcmIntentService.class);
		registrationIntent.putExtra(GcmIntentService.EXTRA_EPL_STMT, eplStmt);
		registrationIntent.putExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID, clientId);
		registrationIntent.putExtra(GcmIntentService.EXTRA_REGISTER, register);
		context.startService(registrationIntent);
	}


	GcmStatus getRegistrationStatus(String eplStmt) {
		return registrationManager.getStatus(eplStmt);
	}


	Set<String> getRegisteredStmts() {
		return registrationManager.getAllObjects(GcmStatus.REGISTERED);
	}


	public static class StatusUpdater extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String eplStmt = intent.getStringExtra(GcmIntentService.EXTRA_EPL_STMT);
			String errorMsg = intent.getStringExtra(GcmIntentService.EXTRA_ERROR_MSG);
			String clientId = intent.getStringExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID);
			boolean register = intent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false);

			// clear pending flag
			if (errorMsg != null) register = !register;

			GcmStatus status = null;
			if (register) status = GcmStatus.REGISTERED;
			else status = GcmStatus.UNREGISTERED;
			GcmManager.getInstance(context).registrationManager.update(eplStmt, clientId, status);

			// send broadcast about changed status
			Intent registrationChangedIntent = new Intent(ACTION_REGISTRATION_STATUS_CHANGED);
			registrationChangedIntent.putExtra(EXTRA_EPL_STMT, eplStmt);
			registrationChangedIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
			registrationChangedIntent.putExtra(EXTRA_REGISTER, register);
			context.sendBroadcast(registrationChangedIntent);
		}

	}

}
