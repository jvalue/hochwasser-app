package de.bitdroid.flooding;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import de.bitdroid.flooding.ods.SyncUtils;

public class MainActivity extends Activity {

	private StationsListAdapter listAdapter;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		listAdapter = new StationsListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.content_list);
		listView.setAdapter(listAdapter);

		SyncUtils.setupSyncAdapter(this);

		getLoaderManager().initLoader(StationsListAdapter.ODS_LOADER_ID, null, listAdapter);
    }

}
