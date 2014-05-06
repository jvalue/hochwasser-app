package de.bitdroid.flooding;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

public class MainActivity extends Activity {

	private static final String AUTHORITY = "de.bitdroid.flooding.provider";
	private static final String ACCOUNT_TYPE = "de.bitdroid.flooding";
	private static final String ACCOUNT = "dummyaccount";
	
	private Account account;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		final StationsListAdapter adapter = new StationsListAdapter(this);
		ListView listView = (ListView) findViewById(R.id.content_list);
		listView.setAdapter(adapter);

		Button updateButton = (Button) findViewById(R.id.update_button);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				adapter.notifyDataSetChanged();
				Bundle bundle = new Bundle();
				bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
				bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
				ContentResolver.requestSync(account, AUTHORITY, bundle); 
			}
		});


		account = createSyncAccount(this);

    }


	private static Account createSyncAccount(Context context) {
		Account account = new Account(ACCOUNT, ACCOUNT_TYPE);
		AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);

		if (accountManager.addAccountExplicitly(account, null, null)) {
			Log.i("Flooding", "Adding account successfull");
		} else {
			Log.i("Flooding", "Adding account failed");
		}
		return account;
	}
}
