package de.bitdroid.flooding;

import android.app.Activity;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import de.bitdroid.flooding.ods.OdsContentProvider;
import de.bitdroid.flooding.ods.SyncSetup;

public class MainActivity extends Activity {

	private StationsListAdapter listAdapter;
	private ContentObserver contentObserver = new ContentObserver(null) {
			@Override
			public void onChange(boolean selfChange) {
				onChange(selfChange, null);
			}
			@Override
			public void onChange(boolean selfChange, Uri uri) {
				listAdapter.notifyDataSetChanged();
			}
		};
	

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
    }


	@Override
	public void onResume() {
		getContentResolver().registerContentObserver(
				OdsContentProvider.CONTENT_URI,
				true,
				contentObserver);

		super.onResume();
	}


	@Override
	public void onPause() {
		getContentResolver().unregisterContentObserver(contentObserver);
		super.onPause();
	}
}
