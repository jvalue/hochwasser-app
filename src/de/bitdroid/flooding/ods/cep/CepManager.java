package de.bitdroid.flooding.ods.cep;

import java.net.URL;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import de.bitdroid.flooding.ods.gcm.GcmRegistrationManager;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.utils.Assert;


public final class CepManager {

	public static final String ACTION_REGISTRATION_STATUS_CHANGED = "de.bitdroid.flooding.ods.cep.ACTION_REGISTRATION_STATUS_CHANGED";

	public static final String
		EXTRA_EPL_STMT = "EXTRA_EPL_STMT",
		EXTRA_REGISTER = "EXTRA_REGISTER",
		EXTRA_ERROR_MSG = "EXTRA_ERROR_MSG";

	private static final String PREFS_NAME = CepManager.class.getName();
	private static final String KEY_SERVER_NAME = "serverName";


	private static CepManager instance;

	public static CepManager getInstance(Context context) {
		Assert.assertNotNull(context);
		if (instance == null) instance = new CepManager(context);
		return instance;
	}


	private final Context context;
	private final GcmRegistrationManager registrationManager;

	private CepManager(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
		this.registrationManager = new GcmRegistrationManager(context, PREFS_NAME);
	}


	/**
	 * Set the name for the CEP server.
	 */
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


	/**
	 * Returns the CEP server name currently being used for all interaction with
	 * the CEP server.
	 */
	public String getCepServerName() {
		return getSharedPreferences().getString(KEY_SERVER_NAME, null);
	}


	/**
	 * Registered a stmt with the CEPS service.
	 */
	public void registerEplStmt(String eplStmt) {
		Assert.assertNotNull(eplStmt);
		GcmStatus status = getRegistrationStatus(eplStmt);
		Assert.assertEquals(status, GcmStatus.UNREGISTERED, "Already registered");

		sourceRegistrationHelper(eplStmt, true);
	}


	/**
	 * Unregisteres a stmt from the CEPS service.
	 */
	public void unregisterEplStmt(String eplStmt) {
		Assert.assertNotNull(eplStmt);
		GcmStatus status = getRegistrationStatus(eplStmt);
		Assert.assertEquals(status, GcmStatus.REGISTERED, "Not registered");

		sourceRegistrationHelper(eplStmt, false);
	}


	private void sourceRegistrationHelper(String eplStmt, boolean register) {
		String clientId =  registrationManager.getClientIdForObjectId(eplStmt);

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


	/**
	 * @return the registration status for this epl stmt.
	 */
	public GcmStatus getRegistrationStatus(String eplStmt) {
		return registrationManager.getStatusForObjectId(eplStmt);
	}


	/**
	 * @return all registered epl stmts.
	 */
	public Set<String> getRegisteredStmts() {
		return registrationManager.getAllObjects(GcmStatus.REGISTERED);
	}


	String getEplStmtForClientId(String clientId) {
		Assert.assertNotNull(clientId);
		for (String stmt : registrationManager.getAllObjects()) {
			if (registrationManager.getClientIdForObjectId(stmt).equals(clientId))
				return stmt;
		}
		return null;
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

			// clear pending flag
			if (errorMsg != null) register = !register;

			GcmStatus status = null;
			if (register) status = GcmStatus.REGISTERED;
			else status = GcmStatus.UNREGISTERED;
			CepManager.getInstance(context).registrationManager.update(eplStmt, clientId, status);

			// send broadcast about changed status
			Intent registrationChangedIntent = new Intent(ACTION_REGISTRATION_STATUS_CHANGED);
			registrationChangedIntent.putExtra(EXTRA_EPL_STMT, eplStmt);
			registrationChangedIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
			registrationChangedIntent.putExtra(EXTRA_REGISTER, register);
			context.sendBroadcast(registrationChangedIntent);
		}

	}


}
