package de.bitdroid.flooding;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.ods.GcmException;
import de.bitdroid.flooding.ods.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;


public final class MainPreferencesActivity extends PreferenceActivity {

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);


		// ods servername
		Preference servername = findPreference(getString(R.string.prefs_ods_servername_key));
		servername.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				try {
					OdsSourceManager
						.getInstance(getApplicationContext())
						.setOdsServerName(newValue.toString());
					return true;
				} catch (IllegalArgumentException iae) {
					Toast.makeText(
						getApplicationContext(), 
						getString(R.string.error_invalid_server_url), 
						Toast.LENGTH_LONG)
						.show();
					return false;
				}
			}
		});


		// source monitoring
		Preference monitoring = findPreference(getString(R.string.prefs_ods_monitor_key));
		monitoring.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				/*
				SourceMonitor monitor = SourceMonitor.getInstance(getApplicationContext());
				boolean start = (Boolean) newValue;
				if (start != monitor.isBeingMonitored(PegelOnlineSource.INSTANCE)) {
					try {
						if (start) monitor.startMonitoring(PegelOnlineSource.INSTANCE);
						else monitor.stopMonitoring(PegelOnlineSource.INSTANCE);
						return true;
					} catch (GcmException ge) {
						Toast.makeText(
								getApplicationContext(), 
								getString(R.string.error_monitoring_failed, ge.getMessage()), 
								Toast.LENGTH_LONG)
							.show();
						return false;
					}
				}
				*/
				return true;
			}
		});
	}

}
