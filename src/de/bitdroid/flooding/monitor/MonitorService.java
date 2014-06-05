package de.bitdroid.flooding.monitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import de.bitdroid.flooding.ods.SyncAdapter;

public final class MonitorService extends Service {

	private final BroadcastReceiver syncListener = new SyncListener();

	@Override
	public IBinder onBind(Intent arg) {
		return null;
	}


	@Override
	public void onCreate() {
		super.onCreate();

		registerReceiver(syncListener, new IntentFilter(SyncAdapter.ACTION_SYNC_FINISH));
	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(syncListener);
	}

	private static class SyncListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "Sync finished!", Toast.LENGTH_LONG).show();
		}
	}
}
