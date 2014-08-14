package de.bitdroid.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.Set;

import de.bitdroid.ods.gcm.GcmStatus;
import de.bitdroid.utils.Assert;
import de.bitdroid.utils.Log;


final class CepManagerImpl implements CepManager {

	private static final String PREFS_NAME = CepManagerImpl.class.getName();
	private static final String KEY_SERVER_NAME = "serverName";

	private static final ObjectMapper mapper = new ObjectMapper();


	private final Context context;
	private final RuleDb ruleDb;

	CepManagerImpl(Context context, RuleDb ruleDb) {
		Assert.assertNotNull(context, ruleDb);
		this.context = context;
		this.ruleDb = ruleDb;
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
	public void registerRule(Rule rule) {
		Assert.assertNotNull(rule);
		GcmStatus status = getRegistrationStatus(rule);
		Assert.assertEquals(status, GcmStatus.UNREGISTERED, "Already registered");

		ruleDb.insert(rule);
		// TODO alert?

		sourceRegistrationHelper(null, rule, true);
	}


	@Override
	public void unregisterRule(Rule rule) {
		Assert.assertNotNull(rule);
		GcmStatus status = getRegistrationStatus(rule);
		Assert.assertEquals(status, GcmStatus.REGISTERED, "Not registered");

		ruleDb.delete(rule);
		// TODO alert?

		String clientId = ruleDb.getClientIdForRule(rule);
		sourceRegistrationHelper(clientId, rule, false);
	}


	private void sourceRegistrationHelper(String clientId, Rule rule, boolean register) {
		// mark task pending
		GcmStatus status = null;
		if (register) status = GcmStatus.PENDING_REGISTRATION;
		else status = GcmStatus.PENDING_UNREGISTRATION;
		ruleDb.updateCepsData(rule, clientId, status);

		// execute in background
		Intent registrationIntent = new Intent(context, GcmIntentService.class);
		registrationIntent.putExtra(GcmIntentService.EXTRA_RULE, rule);
		registrationIntent.putExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID, clientId);
		registrationIntent.putExtra(GcmIntentService.EXTRA_REGISTER, register);
		context.startService(registrationIntent);
	}


	@Override
	public GcmStatus getRegistrationStatus(Rule rule) {
		GcmStatus status = ruleDb.getStatusForRule(rule);
		if (status == null) return GcmStatus.UNREGISTERED;
		return status;
	}


	@Override
	public Rule getRuleForClientId(String clientId) {
		return ruleDb.getRuleForClientId(clientId);
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


	@Override
	public Set<Rule> getAll() {
		return ruleDb.getAll();
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}


	public static class StatusUpdater extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Rule rule = intent.getParcelableExtra(GcmIntentService.EXTRA_RULE);
			String errorMsg = intent.getStringExtra(GcmIntentService.EXTRA_ERROR_MSG);
			String clientId = intent.getStringExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID);
			boolean register = intent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false);


			// abort if stmt was registered on server, but not on client
			if (rule == null && !register) {
				Log.info("not forwarding unregistration action since ruleJson was null");
				return;
			}

			// clear pending flag
			if (errorMsg != null) register = !register;

			GcmStatus status = null;
			if (register) status = GcmStatus.REGISTERED;
			else status = GcmStatus.UNREGISTERED;
			((CepManagerImpl) CepManagerFactory.createCepManager(context))
					.ruleDb.updateCepsData(rule, clientId, status);

			// send broadcast about changed status
			Intent registrationChangedIntent = new Intent(ACTION_REGISTRATION_STATUS_CHANGED);
			registrationChangedIntent.putExtra(EXTRA_RULE, rule);
			registrationChangedIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
			registrationChangedIntent.putExtra(EXTRA_REGISTER, register);
			context.sendBroadcast(registrationChangedIntent);
		}

	}

}
