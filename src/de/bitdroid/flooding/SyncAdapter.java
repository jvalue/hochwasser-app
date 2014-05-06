package de.bitdroid.flooding;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public final class SyncAdapter extends AbstractThreadedSyncAdapter {

	private int syncCounter = 0;


	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
	}

	public SyncAdapter(
			Context context,
			boolean autoInitialize,
			boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
	}

	@Override
	public void onPerformSync(
			Account account,
			Bundle extras,
			String authority,
			ContentProviderClient provider,
			SyncResult syncResult) {



		syncCounter++;
		String msg = "SyncCounter = " + syncCounter;

		Uri uri = new Uri.Builder().scheme("content").authority(authority).path("foobar").build();
		ContentValues data = new ContentValues();
		data.put(StubProvider.KEY, msg);

		try {
			provider.insert(uri, data);
		} catch (RemoteException re) {
			syncResult.hasHardError();
		}

		Log.i("Flooding", "synced");
	}
}
