package de.bitdroid.flooding;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import de.bitdroid.flooding.ods.OdsSourceManager;


public final class MainPreferencesActivity extends PreferenceActivity 
	implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}


	@Override
	public void onResume() {
		super.onResume();
		PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext())
			.registerOnSharedPreferenceChangeListener(this);
	}


	@Override
	public void onPause() {
		super.onPause();
		PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext())
			.unregisterOnSharedPreferenceChangeListener(this);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		if (key.equals(getString(R.string.prefs_ods_servername_key))) {
			String serverName = prefs.getString(key, null);
			OdsSourceManager.getInstance(getApplicationContext()).setOdsServerName(serverName);
		}
	}

}
