package de.bitdroid.flooding;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import de.bitdroid.flooding.levels.ChooseRiverActivity;
import de.bitdroid.flooding.map.MapActivity;
import de.bitdroid.flooding.monitor.SourceMonitor;
import de.bitdroid.flooding.ods.AbstractGcmRegistrationReceiver;
import de.bitdroid.flooding.ods.GcmStatus;
import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.ods.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;

public class MainActivity extends Activity {

	private StationsListAdapter listAdapter;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		// OSM
		Button mapButton = (Button) findViewById(R.id.map_button);
		mapButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(
					MainActivity.this.getApplicationContext(), 
					MapActivity.class);
				startActivity(intent);
			}
		});


		// water levels
		Button levelsButton = (Button) findViewById(R.id.levels_button);
		levelsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(
					MainActivity.this.getApplicationContext(),
					ChooseRiverActivity.class);
				startActivity(intent);
			}
		});


		// simple stations list
		listAdapter = new StationsListAdapter(getApplicationContext());
		ListView listView = (ListView) findViewById(R.id.content_list);
		listView.setAdapter(listAdapter);

		getLoaderManager().initLoader(
				StationsListAdapter.LOADER_ID,
				null, 
				listAdapter.getLoaderCallback());


		// ODS source setup
		OdsSourceManager sourceManager = OdsSourceManager.getInstance(getApplicationContext());
		if (sourceManager.getOdsServerName() == null) {
			PreferenceManager.setDefaultValues(
					getApplicationContext(),
					R.xml.preferences,
					false);
			sourceManager.setOdsServerName(
					PreferenceManager
						.getDefaultSharedPreferences(getApplicationContext())
						.getString(getString(R.string.prefs_ods_servername_key), null));
		}
		if (!sourceManager.isPollingActive()) {
			sourceManager.startManualSync(PegelOnlineSource.INSTANCE);
			sourceManager.startPolling(1000 * 60 * 60, PegelOnlineSource.INSTANCE);
		}


		// monitor setup (should only execute on first run)
		checkForSourceMonitor(PegelOnlineSource.INSTANCE);
    }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch(menuItem.getItemId()) { 
			case R.id.select_settings:
				Intent settingsIntent = new Intent(
						getApplicationContext(),
						MainPreferencesActivity.class);
				startActivity(settingsIntent);
				return true;

			case R.id.select_about:
				OdsSourceManager manager = OdsSourceManager.getInstance(getApplicationContext());

				new AlertDialog.Builder(this)
					.setTitle(R.string.main_dialog_info_title)
					.setMessage(getString(R.string.main_dialog_info_msg,
								formatTime(manager.getLastSync(PegelOnlineSource.INSTANCE)),
								formatTime(manager.getLastSuccessfulSync(PegelOnlineSource.INSTANCE)),
								formatTime(manager.getLastFailedSync(PegelOnlineSource.INSTANCE))))
					.setPositiveButton(R.string.btn_ok, null)
					.show();

				return true;
		}
		return false;
	}


	@Override
	public void onResume() {
		super.onResume();
		if (!arePlayServicesAvailable()) finish();
	}


	private boolean arePlayServicesAvailable() {
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		if (status == ConnectionResult.SUCCESS) return true;

		if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 42);
			dialog.show();
		}

		return false;
	}


	private void checkForSourceMonitor(OdsSource source) {
		boolean enabled = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext())
			.getBoolean(getString(R.string.prefs_ods_monitor_key), false);

		SourceMonitor monitor = SourceMonitor.getInstance(getApplicationContext());
		if (enabled && !monitor.isBeingMonitored(source)) {
			monitor.startMonitoring(source);
		}

		OdsSourceManager manager = OdsSourceManager.getInstance(getApplicationContext());
		GcmStatus status = manager.getPushNotificationsRegistrationStatus(source);
		if (!status.equals(GcmStatus.REGISTERED)) {
			registerReceiver(gcmReceiver, AbstractGcmRegistrationReceiver.getIntentFilter());
			manager.startPushNotifications(source);
		}
	}


	private final static SimpleDateFormat dateFormatter 
		= new SimpleDateFormat("dd/M/yyyy hh:mm a");

	private String formatTime(Calendar time) {
		if (time == null) return getString(R.string.main_dialog_info_never);
		else return dateFormatter.format(time.getTime());
	}


	private static final BroadcastReceiver gcmReceiver = new AbstractGcmRegistrationReceiver() {
		@Override
		public void onReceive(
				Context context, 
				OdsSource source, 
				boolean register, 
				String errorMsg) {

			Toast.makeText(
					context, 
					"source = " + source + "\nerrorMsg = " 
					+ errorMsg + "\nregister = " + register, 
					Toast.LENGTH_LONG)
				.show();
		}
	};
}
