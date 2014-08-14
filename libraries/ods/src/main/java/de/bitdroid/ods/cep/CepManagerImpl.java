package de.bitdroid.ods.cep;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import de.bitdroid.ods.gcm.GcmRegistrationManager;
import de.bitdroid.ods.gcm.GcmStatus;
import de.bitdroid.utils.Assert;
import de.bitdroid.utils.Log;

import static de.bitdroid.ods.cep.RuleDbSchema.COLUMN_JSON;
import static de.bitdroid.ods.cep.RuleDbSchema.TABLE_NAME;


final class CepManagerImpl implements CepManager {

	private static final String PREFS_NAME = CepManagerImpl.class.getName();
	private static final String KEY_SERVER_NAME = "serverName";

	private static final ObjectMapper mapper = new ObjectMapper();


	private final Context context;
	private final GcmRegistrationManager registrationManager;
	private final RuleDb ruleDb;

	CepManagerImpl(Context context, RuleDb ruleDb) {
		Assert.assertNotNull(context, ruleDb);
		this.context = context;
		this.registrationManager = new GcmRegistrationManager(context, PREFS_NAME);
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

		sourceRegistrationHelper(null, rule, true);

		// store in db
		SQLiteDatabase database = null;
		try {
			database = ruleDb.getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(COLUMN_JSON, ((Object) mapper.valueToTree(rule)).toString());
			database.insert(TABLE_NAME, null, values);
			// TODO alert?
		} finally {
			if (database != null) database.close();
		}
	}


	@Override
	public void unregisterRule(Rule rule) {
		Assert.assertNotNull(rule);
		GcmStatus status = getRegistrationStatus(rule);
		Assert.assertEquals(status, GcmStatus.REGISTERED, "Not registered");

		String clientId =  registrationManager.getClientIdForObjectId(rule.getUuid());
		sourceRegistrationHelper(clientId, rule, false);

		// remove from db
		SQLiteDatabase database = null;
		try {
			database = ruleDb.getWritableDatabase();
			database.delete(
					TABLE_NAME,
					COLUMN_JSON + "=?",
					new String[]{((Object) mapper.valueToTree(rule)).toString()});
		} finally {
			if (database != null) database.close();
		}
	}


	private void sourceRegistrationHelper(String clientId, Rule rule, boolean register) {
		// mark task pending
		GcmStatus status = null;
		if (register) status = GcmStatus.PENDING_REGISTRATION;
		else status = GcmStatus.PENDING_UNREGISTRATION;
		registrationManager.update(rule.getUuid(), clientId, status);

		// execute in background
		Intent registrationIntent = new Intent(context, GcmIntentService.class);
		registrationIntent.putExtra(GcmIntentService.EXTRA_RULE, rule);
		registrationIntent.putExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID, clientId);
		registrationIntent.putExtra(GcmIntentService.EXTRA_REGISTER, register);
		context.startService(registrationIntent);
	}


	// TODO registration status + clientId are kept in SharedPreferences, while rule is in an
	// sqlite db --> big error potential
	@Override
	public GcmStatus getRegistrationStatus(Rule rule) {
		return registrationManager.getStatusForObjectId(rule.getUuid());
	}


	@Override
	public Rule getRuleForClientId(String clientId) {
		// TODO performance??
		Assert.assertNotNull(clientId);
		String ruleId = registrationManager.getObjectIdForClientId(clientId);
		for (Rule rule : getAll()) {
			if (rule.getUuid().equals(ruleId)) return rule;
		}
		return null;
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
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(TABLE_NAME);
		String[] columns = { COLUMN_JSON };
		Cursor cursor = builder.query(
				ruleDb.getReadableDatabase(),
				columns,
				null, null, null, null, null);

		Set<Rule> rules = new HashSet<Rule>();

		if (cursor.getCount() <= 0) return rules;
		cursor.moveToFirst();
		do {
			String json = cursor.getString(0);
			try {
				Rule rule = mapper.treeToValue(mapper.readTree(json), Rule.class);;
				rules.add(rule);
			} catch (Exception e) {
				Log.error("failed to read rule from db", e);
			}
		} while (cursor.moveToNext());

		return rules;
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
			((CepManagerImpl) CepManagerFactory.createCepManager(context)).registrationManager.update(rule.getUuid(), clientId, status);

			// send broadcast about changed status
			Intent registrationChangedIntent = new Intent(ACTION_REGISTRATION_STATUS_CHANGED);
			registrationChangedIntent.putExtra(EXTRA_RULE, rule);
			registrationChangedIntent.putExtra(EXTRA_ERROR_MSG, errorMsg);
			registrationChangedIntent.putExtra(EXTRA_REGISTER, register);
			context.sendBroadcast(registrationChangedIntent);
		}

	}

}
