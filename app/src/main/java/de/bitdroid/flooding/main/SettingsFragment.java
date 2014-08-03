package de.bitdroid.flooding.main;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.preference.PreferenceFragment;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.ods.cep.CepManagerFactory;
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
				CepManagerFactory.createCepManager(getActivity().getApplicationContext()).setCepServerName(newServerName);
			}
		});

		// source monitoring
		Preference monitoring = findPreference(getString(R.string.prefs_ods_monitor_key));
		final EditTextPreference monitorDuration
				= (EditTextPreference) findPreference(getString(R.string.prefs_ods_monitor_days_key));
		final Preference monitorInterval = findPreference(getString(R.string.prefs_ods_monitor_interval_key));

		monitoring.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SourceMonitor monitor = SourceMonitor
						.getInstance(getActivity().getApplicationContext());
				OdsSourceManager sourceManager = OdsSourceManager
						.getInstance(getActivity().getApplication());
				OdsSource source = PegelOnlineSource.INSTANCE;

				boolean startMonitor = (Boolean) newValue;
				if (startMonitor) {
					if (!monitor.isBeingMonitored(source)) monitor.startMonitoring(source);
					double intervalInHours = Double.valueOf(getString(R.string.prefs_ods_monitor_interval_default));
					long interval = (long) (intervalInHours * 60 * 60);

					if (!sourceManager.isRegisteredForPolling(source)) sourceManager.startPolling(interval, source);
				} else {
					if (monitor.isBeingMonitored(source)) monitor.stopMonitoring(source);
					if (sourceManager.isRegisteredForPolling(source)) sourceManager.stopPolling();
				}

				return true;
			}
		});

		int days = Integer.valueOf(monitorDuration.getText().toString());
		monitorDuration.setSummary(getString(R.string.prefs_ods_monitor_days_format, days));
		monitorDuration.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				int newDays = Integer.valueOf(newValue.toString());
				if (newDays <= 0) return false;

				monitorDuration.setSummary(getString(R.string.prefs_ods_monitor_days_format, newDays));
				return true;
			}
		});

		double intervalValue = Double.valueOf(getString(R.string.prefs_ods_monitor_interval_default));
		monitorInterval.setSummary(getString(R.string.prefs_ods_monitor_interval_format, intervalValue));
		monitorInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				Toast.makeText(getActivity(), "stub", Toast.LENGTH_SHORT).show();
				return false;
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
