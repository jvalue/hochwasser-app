package de.bitdroid.flooding.data;

import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import java.util.List;

import de.bitdroid.utils.Assert;
import timber.log.Timber;

import static de.bitdroid.flooding.data.OdsSource.ACCOUNT;
import static de.bitdroid.flooding.data.OdsSource.AUTHORITY;


public class SyncUtils {

	private static final String
			KEY_ACCOUNT_ADDED = "accountAdded",
			KEY_PERIODIC_SYNC_ADDED = "periodicSyncAdded",
			KEY_PERIODIC_SYNC_SERVERNAME = "serverName",
			KEY_PERIODIC_SYNC_JSON = "sourceJson";

	private static final String PREFS_NAME = SyncUtils.class.getName();


	private final Context context;

	public SyncUtils(Context context) {
		Assert.assertNotNull(context);
		this.context = context;
	}

	public boolean isPeriodicSyncScheduled() {
		return getSharedPreferences().getBoolean(KEY_PERIODIC_SYNC_ADDED, false);
	}


	public void startPeriodicSync(
				String odsServerName,
				List<OdsSource> sources,
				long pollFrequency,
				boolean wifiOnly) {

		// adapter specific
		ArrayNode json = new ArrayNode(JsonNodeFactory.instance);
		for (OdsSource source : sources) json.add(source.toString());
		Bundle settingsBundle = getSyncExtras(odsServerName, json.toString(), wifiOnly);

		ContentResolver.setIsSyncable(ACCOUNT, AUTHORITY, 1);
		ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, true);
		ContentResolver.addPeriodicSync(
				ACCOUNT,
				AUTHORITY,
				settingsBundle,
				pollFrequency);

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean(KEY_PERIODIC_SYNC_ADDED, true);
		editor.putString(KEY_PERIODIC_SYNC_SERVERNAME, odsServerName);
		editor.putString(KEY_PERIODIC_SYNC_JSON, json.toString());
		editor.commit();
	}


	public void stopPeriodicSync() {
		SharedPreferences prefs = getSharedPreferences();
		String odsServerName = prefs.getString(KEY_PERIODIC_SYNC_SERVERNAME, null);
		String jsonSources = prefs.getString(KEY_PERIODIC_SYNC_JSON, null);
		Bundle settingsBundle = getSyncExtras(odsServerName, jsonSources, false);

		ContentResolver.setSyncAutomatically(ACCOUNT, AUTHORITY, false);
		ContentResolver.removePeriodicSync(ACCOUNT, AUTHORITY, settingsBundle);

		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(KEY_PERIODIC_SYNC_ADDED, false);
		editor.remove(KEY_PERIODIC_SYNC_SERVERNAME);
		editor.remove(KEY_PERIODIC_SYNC_JSON);
		editor.commit();
	}


	public void startManualSync(String odsServerName, OdsSource source) {
		// adapter specific
		ArrayNode json = new ArrayNode(JsonNodeFactory.instance);
		json.add(source.toString());
		Bundle settingsBundle = getSyncExtras(odsServerName, json.toString(), false);

		settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

		ContentResolver.requestSync(
				OdsSource.ACCOUNT,
				OdsSource.AUTHORITY,
				settingsBundle);
	}


	public void addAccount() {
		AccountManager accountManager 
			= (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

		if (!accountManager.addAccountExplicitly(ACCOUNT, null, null)) {
			Timber.d("Adding account failed");
		}

		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean(KEY_ACCOUNT_ADDED,  true);
		editor.commit();
	}


	public boolean isAccountAdded() {
		SharedPreferences prefs = getSharedPreferences();
		return prefs.getBoolean(KEY_ACCOUNT_ADDED, false);
	}


	private Bundle getSyncExtras(String odsServerName, String jsonSources, boolean wifiOnly) {
		Bundle settingsBundle = new Bundle();
		settingsBundle.putString(SyncAdapter.EXTRA_ODS_URL, odsServerName);
		settingsBundle.putString(SyncAdapter.EXTRA_SOURCE_JSON, jsonSources);
		settingsBundle.putBoolean(SyncAdapter.EXTRA_WIFI_ONLY, wifiOnly);
		return settingsBundle;
	}

	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

}
