package de.bitdroid.flooding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
}
