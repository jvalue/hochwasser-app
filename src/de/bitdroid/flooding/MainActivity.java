package de.bitdroid.flooding;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import de.bitdroid.flooding.ods.OdsContentProvider;
import de.bitdroid.flooding.ods.OdsTable;
import de.bitdroid.flooding.utils.Log;

public class MainActivity extends Activity {

	private static final String ACCOUNT_TYPE = "de.bitdroid.flooding";
	private static final String ACCOUNT = "dummyaccount";

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

		createSyncAccount(this);


		// trigger a sync manually
		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(
					OdsContentProvider.CONTENT_URI.buildUpon()
						.appendPath("sync")
						.build(),
					new String[] { OdsTable.COLUMN_SERVER_ID },
					null, null, null);
		} finally {
			cursor.close();
		}
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



	private static Account createSyncAccount(Context context) {
		Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
		AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

		if (accountManager.addAccountExplicitly(account, null, null)) {
			Log.info("Adding account successfull");
		} else {
			Log.warning("Adding account failed");
		}
		return account;
	}
}
