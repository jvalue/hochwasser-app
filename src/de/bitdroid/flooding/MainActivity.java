package de.bitdroid.flooding;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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


		// gcm test 
		Button gcmButton = (Button) findViewById(R.id.gcm_button);
		gcmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(
					MainActivity.this.getApplicationContext(),
					GcmTestActivity.class);
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
		sourceManager.setOdsServerName("http://192.168.178.93:8182");
		PegelOnlineSource source = new PegelOnlineSource();
		if (!sourceManager.isPollingActive()) {
			sourceManager.startManualSync(source);
			sourceManager.startPolling(1000 * 60 * 60, source);
		}


		// testing
		SourceMonitor monitor = SourceMonitor.getInstance(getApplicationContext());
		if (monitor.isBeingMonitored(source)) {
			Toast.makeText(this, "Already monitoring", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, "Starting monitoring", Toast.LENGTH_SHORT).show();
			monitor.startMonitoring(source);
		}
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

}
