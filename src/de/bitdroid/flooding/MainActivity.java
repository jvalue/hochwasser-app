package de.bitdroid.flooding;

import android.app.Activity;
import android.app.LoaderManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import de.bitdroid.flooding.ods.SyncSetup;

public class MainActivity extends Activity {

	private StationsListAdapter listAdapter;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		listAdapter = new StationsListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.content_list);
		listView.setAdapter(listAdapter);

		Button updateButton = (Button) findViewById(R.id.update_button);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listAdapter.notifyDataSetChanged();
			}
		});

		SyncSetup.setupSyncAdapter(this);

		getLoaderManager().initLoader(StationsListAdapter.ODS_LOADER_ID, null, listAdapter);
    }

}
