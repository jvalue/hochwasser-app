package de.bitdroid.flooding.main;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.ods.cep.CepManager;
import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.flooding.ods.data.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;


public final class SettingsFragment extends PreferenceFragment {



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// server names
		updateServerName(R.string.prefs_ods_servername_key, new ChangeServerName() {
			@Override
			public void changeServerName(String newServerName) {
				OdsSourceManager
					.getInstance(getActivity().getApplicationContext())
					.setOdsServerName(newServerName);
			}
		});
		updateServerName(R.string.prefs_ceps_servername_key, new ChangeServerName() {
			@Override
			public void changeServerName(String newServerName) {
				CepManager
					.getInstance(getActivity().getApplicationContext())
					.setCepServerName(newServerName);
			}
		});

		// source monitoring
		Preference monitoring = findPreference(getString(R.string.prefs_ods_monitor_key));
		monitoring.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SourceMonitor monitor = SourceMonitor
					.getInstance(getActivity().getApplicationContext());
				OdsSource source = PegelOnlineSource.INSTANCE;

				if (!monitor.isBeingMonitored(source)) monitor.startMonitoring(source);
				else monitor.stopMonitoring(source);

				return true;
			}
		});

		// sync status
		OdsSourceManager manager 
			= OdsSourceManager.getInstance(getActivity().getApplicationContext());

		Preference lastSync = findPreference(getString(R.string.prefs_ods_sync_last_key));
		lastSync.setSummary(formatTime(manager.getLastSync(PegelOnlineSource.INSTANCE)));

		Preference successSync = findPreference(getString(R.string.prefs_ods_sync_last_success_key));
		successSync.setSummary(formatTime(manager.getLastSuccessfulSync(PegelOnlineSource.INSTANCE)));

		Preference failSync = findPreference(getString(R.string.prefs_ods_sync_last_fail_key));
		failSync.setSummary(formatTime(manager.getLastFailedSync(PegelOnlineSource.INSTANCE)));
	}


	private final static SimpleDateFormat dateFormatter
		= new SimpleDateFormat("dd/M/yyyy hh:mm a");
	
	private String formatTime(Calendar time) {
		if (time == null) return getString(R.string.never);
		else return dateFormatter.format(time.getTime());
	}


	private void updateServerName(int prefsViewId, final ChangeServerName serverNameChanger) {
		final EditTextPreference servername 
			= (EditTextPreference) findPreference(getString(prefsViewId));
		servername.setSummary(servername.getText());
		servername.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String newServerName = newValue.toString();
				try {
					serverNameChanger.changeServerName(newServerName);
					servername.setSummary(newServerName);
					return true;
				} catch (Exception e) {
					Toast.makeText(
						getActivity().getApplicationContext(), 
						getString(R.string.error_invalid_server_url), 
						Toast.LENGTH_LONG)
						.show();
					return false;
				}
			}
		});
	}



	private static interface ChangeServerName {

		public void changeServerName(String newServerName) throws Exception;

	}

}
