package de.bitdroid.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.bitdroid.ods.gcm.GcmStatus;
import de.bitdroid.utils.Assert;


final class CepManagerImpl implements CepManager {

	private static final String PREFS_NAME = CepManagerImpl.class.getName();
	private static final String KEY_SERVER_NAME = "serverName";


	private final Context context;
	private final RuleDb ruleDb;
	private final List<RuleUpdateListener> listeners = new LinkedList<RuleUpdateListener>();

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
		Assert.assertTrue(status.equals(GcmStatus.ERROR_REGISTRATION) || status.equals(GcmStatus.UNREGISTERED), "Already registered");

		if (status.equals(GcmStatus.UNREGISTERED)) ruleDb.insert(rule);

		sourceRegistrationHelper(null, rule, true);
	}


	@Override
	public void unregisterRule(Rule rule) {
		Assert.assertNotNull(rule);
		GcmStatus status = getRegistrationStatus(rule);
		Assert.assertTrue(
				status.equals(GcmStatus.ERROR_UNREGISTRATION)
						|| status.equals(GcmStatus.REGISTERED)
						|| status.equals(GcmStatus.ERROR_REGISTRATION),
				"Not registered"
		);

		if  (status.equals(GcmStatus.ERROR_REGISTRATION)) {
			updateCepsData(rule, null, GcmStatus.UNREGISTERED);
		} else {
			String clientId = ruleDb.getClientIdForRule(rule);
			sourceRegistrationHelper(clientId, rule, false);
		}
	}


	private void sourceRegistrationHelper(String clientId, Rule rule, boolean register) {
		// mark task pending
		GcmStatus status;
		if (register) status = GcmStatus.PENDING_REGISTRATION;
		else status = GcmStatus.PENDING_UNREGISTRATION;
		ruleDb.updateCepsData(rule, clientId, status);
		alertListeners(rule, status);

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
	public Set<Rule> getAllRules() {
		return ruleDb.getAll();
	}


	@Override
	public void registerRuleUpdateListener(RuleUpdateListener listener) {
		Assert.assertNotNull(listener);
		listeners.add(listener);
	}


	@Override
	public void unregisterRuleUpdateListener(RuleUpdateListener listener) {
		Assert.assertNotNull(listener);
		listeners.remove(listener);
	}


	private void alertListeners(Rule rule, GcmStatus status) {
		for (RuleUpdateListener listener : listeners) {
			listener.onStatusChanged(rule, status);
		}
	}

	private void updateCepsData(Rule rule, String clientId, GcmStatus status) {
		if (status.equals(GcmStatus.UNREGISTERED)) ruleDb.delete(rule);
		else ruleDb.updateCepsData(rule, clientId, status);
		alertListeners(rule, status);
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

			// determine status
			GcmStatus status;
			if (errorMsg != null) {
				if (register) status = GcmStatus.ERROR_REGISTRATION;
				else status = GcmStatus.ERROR_UNREGISTRATION;
			} else {
				if (register) status = GcmStatus.REGISTERED;
				else status = GcmStatus.UNREGISTERED;
			}

			// update status
			((CepManagerImpl) CepManagerFactory.createCepManager(context))
					.updateCepsData(rule, clientId, status);

			// send broadcast about changed status
			Intent registrationChangedIntent = new Intent(ACTION_REGISTRATION_STATUS_CHANGED);
			registrationChangedIntent.putExtra(EXTRA_RULE, rule);
			registrationChangedIntent.putExtra(EXTRA_STATUS, status.name());
			registrationChangedIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
			context.sendBroadcast(registrationChangedIntent);
		}

	}

}
