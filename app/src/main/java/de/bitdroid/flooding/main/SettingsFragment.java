package de.bitdroid.flooding.main;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.support.v4.preference.PreferenceFragment;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.Calendar;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.ods.cep.CepManagerFactory;
import de.bitdroid.ods.data.OdsSource;
import de.bitdroid.ods.data.OdsSourceManager;
import de.bitdroid.utils.Log;


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
		final CheckBoxPreference wifiOnlySync = (CheckBoxPreference) findPreference(getString(R.string.prefs_ods_monitor_wifi_key));

		monitoring.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SourceMonitor monitor = SourceMonitor
						.getInstance(getActivity().getApplicationContext());
				OdsSourceManager sourceManager = OdsSourceManager
						.getInstance(getActivity().getApplicationContext());
				OdsSource source = PegelOnlineSource.INSTANCE;

				boolean startMonitor = (Boolean) newValue;
				if (startMonitor) {
					if (!monitor.isBeingMonitored(source)) monitor.startMonitoring(source);
					double intervalInHours = Double.valueOf(getString(R.string.prefs_ods_monitor_interval_default));
					long interval = (long) (intervalInHours * 60 * 60);

					if (!sourceManager.isRegisteredForPolling(source))
						sourceManager.startPolling(interval, wifiOnlySync.isChecked(), source);
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

		// about - version
		final Preference versionPref = findPreference(getString(R.string.prefs_about_version_key));
		try {
			String versionName = getActivity()
					.getPackageManager()
					.getPackageInfo(getActivity().getPackageName(), 0)
					.versionName;
			versionPref.setSummary(versionName);
		} catch (PackageManager.NameNotFoundException nnfe) {
			Crashlytics.logException(nnfe);
			Log.error("failed to get package name", nnfe);
		}

		// about - feedback
		Preference feebackPref = findPreference(getString(R.string.prefs_about_feedback_key));
		feebackPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				String address = getString(R.string.feedback_mail_address);
				String subject = getString(
						R.string.feedback_mail_subject,
						getString(R.string.app_name),
						versionPref.getSummary().toString());
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null));
				intent.putExtra(Intent.EXTRA_SUBJECT, subject);
				startActivity(Intent.createChooser(intent, getString(R.string.feedback_mail_chooser)));
				return false;
			}
		});

	}


	private String formatTime(Calendar time) {
		if (time == null) return getString(R.string.never);
		else return DateFormat.getDateFormat(getActivity()).format(time.getTime())
			+ " " + DateFormat.getTimeFormat(getActivity()).format(time.getTime());
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
