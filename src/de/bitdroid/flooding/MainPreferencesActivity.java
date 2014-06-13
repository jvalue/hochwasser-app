package de.bitdroid.flooding;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.ods.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;


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
		if (updateServerName(prefs, key)) return;
		if (updateMonitoring(prefs, key)) return;
	}


	private boolean updateServerName(SharedPreferences prefs, String key) {
		if (key.equals(getString(R.string.prefs_ods_servername_key))) {
			String serverName = prefs.getString(key, null);
			OdsSourceManager.getInstance(getApplicationContext()).setOdsServerName(serverName);
			return true;
		}
		return false;
	}


	private boolean updateMonitoring(SharedPreferences prefs, String key) {
		if (key.equals(getString(R.string.prefs_ods_monitor_key))) {
			SourceMonitor monitor = SourceMonitor.getInstance(getApplicationContext());
			PegelOnlineSource source = new PegelOnlineSource();
			boolean start = prefs.getBoolean(key, false);
			if (start != monitor.isBeingMonitored(source)) {
				if (start) monitor.startMonitoring(source);
				else monitor.stopMonitoring(source);
			}
			return true;
		}
		return false;
	}

}
