package de.bitdroid.flooding;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import de.bitdroid.flooding.map.MapActivity;
import de.bitdroid.flooding.ods.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelonlineTableAdapter;

public class MainActivity extends Activity {

	private StationsListAdapter listAdapter;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		Button mapButton = (Button) findViewById(R.id.map_button);
		mapButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(
					MainActivity.this.getApplicationContext(), 
					MapActivity.class);
				startActivity(intent);
			}
		});

		listAdapter = new StationsListAdapter(getApplicationContext());
		ListView listView = (ListView) findViewById(R.id.content_list);
		listView.setAdapter(listAdapter);

		OdsSourceManager sourceManager = OdsSourceManager.getInstance();
		sourceManager.startMonitoring(getApplicationContext());
		sourceManager.registerSource(PegelonlineTableAdapter.class);

		getLoaderManager().initLoader(
				StationsLoaderCallbacks.ODS_LOADER_ID, 
				null, 
				listAdapter.getLoaderCallback());
    }

}
