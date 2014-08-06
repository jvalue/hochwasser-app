package de.bitdroid.flooding.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.net.URL;
import java.util.Set;

import de.bitdroid.flooding.ods.gcm.GcmRegistrationManager;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.utils.Assert;
import de.bitdroid.utils.Log;


final class CepManagerImpl implements CepManager {

	private static final String PREFS_NAME = CepManagerImpl.class.getName();
	private static final String KEY_SERVER_NAME = "serverName";


	private final Context context;
	private final GcmRegistrationManager registrationManager;

	CepManagerImpl(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
		this.registrationManager = new GcmRegistrationManager(context, PREFS_NAME);
	}


	@Override
	public void setCepServerName(String cepServerName) {
		Assert.assertNotNull(cepServerName);
		try {
			URL checkUrl = new URL(cepServerName);
			checkUrl.toURI();
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(KEY_SERVER_NAME, cepServerName);
		editor.commit();
	}


	@Override
	public String getCepServerName() {
		return getSharedPreferences().getString(KEY_SERVER_NAME, null);
	}


	@Override
	public void registerEplStmt(String eplStmt) {
		Assert.assertNotNull(eplStmt);
		GcmStatus status = getRegistrationStatus(eplStmt);
		Assert.assertEquals(status, GcmStatus.UNREGISTERED, "Already registered");

		sourceRegistrationHelper(null, eplStmt, true);
	}


	@Override
	public void unregisterEplStmt(String eplStmt) {
		Assert.assertNotNull(eplStmt);
		GcmStatus status = getRegistrationStatus(eplStmt);
		Assert.assertEquals(status, GcmStatus.REGISTERED, "Not registered");

		String clientId =  registrationManager.getClientIdForObjectId(eplStmt);
		sourceRegistrationHelper(clientId, eplStmt, false);
	}


	private void sourceRegistrationHelper(String clientId, String eplStmt, boolean register) {
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


	@Override
	public GcmStatus getRegistrationStatus(String eplStmt) {
		return registrationManager.getStatusForObjectId(eplStmt);
	}


	@Override
	public Set<String> getRegisteredStmts() {
		return registrationManager.getAllObjects(GcmStatus.REGISTERED);
	}


	@Override
	public String getEplStmtForClientId(String clientId) {
		Assert.assertNotNull(clientId);
		return registrationManager.getObjectIdForClientId(clientId);
	}


	@Override
	public void unregisterClientId(String clientId) {
		Assert.assertNotNull(clientId);

		// execute in background
		Intent registrationIntent = new Intent(context, GcmIntentService.class);
		registrationIntent.putExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID, clientId);
		registrationIntent.putExtra(GcmIntentService.EXTRA_REGISTER, false);
		context.startService(registrationIntent);
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}


	public static class StatusUpdater extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String eplStmt = intent.getStringExtra(GcmIntentService.EXTRA_EPL_STMT);
			String errorMsg = intent.getStringExtra(GcmIntentService.EXTRA_ERROR_MSG);
			String clientId = intent.getStringExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID);
			boolean register = intent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false);

			// abort if stmt was registered on server, but not on client
			if (eplStmt == null && !register) {
				Log.info("not forwarding unregistration action since eplStmt was null");
				return;
			}

			// clear pending flag
			if (errorMsg != null) register = !register;

			GcmStatus status = null;
			if (register) status = GcmStatus.REGISTERED;
			else status = GcmStatus.UNREGISTERED;
			((CepManagerImpl) CepManagerFactory.createCepManager(context)).registrationManager.update(eplStmt, clientId, status);

			// send broadcast about changed status
			Intent registrationChangedIntent = new Intent(ACTION_REGISTRATION_STATUS_CHANGED);
			registrationChangedIntent.putExtra(EXTRA_EPL_STMT, eplStmt);
			registrationChangedIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
			registrationChangedIntent.putExtra(EXTRA_REGISTER, register);
			context.sendBroadcast(registrationChangedIntent);
		}

	}


}
