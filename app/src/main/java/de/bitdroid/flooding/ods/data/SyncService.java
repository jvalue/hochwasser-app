package de.bitdroid.flooding.ods.data;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public final class SyncService extends Service {

	private static SyncAdapter syncAdapter = null;
	private static final Object lock = new Object();

	@Override
	public void onCreate() {
		synchronized(lock) {
			if (syncAdapter == null)
				syncAdapter = new SyncAdapter(getApplicationContext(), true);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return syncAdapter.getSyncAdapterBinder();
	}
}

