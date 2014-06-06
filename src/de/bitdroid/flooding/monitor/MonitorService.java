package de.bitdroid.flooding.monitor;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.ods.SyncAdapter;

public final class MonitorService extends Service {

	public static final String 
			EXTRA_SOURCE_NAME = "EXTRA_SOURCE_NAME",
			EXTRA_REGISTER_SOURCE = "EXTRA_REGISTER_SOURCE";

	private final Map<String, OdsSource> sources = new HashMap<String, OdsSource>();
	private BroadcastReceiver syncListener;


	@Override
	public IBinder onBind(Intent arg) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String sourceName = intent.getExtras().getString(EXTRA_SOURCE_NAME);
		boolean register = intent.getExtras().getBoolean(EXTRA_REGISTER_SOURCE);

		if (register) {
			OdsSource source = OdsSource.fromClassName(sourceName);
			sources.put(sourceName, source);

			// first source added?
			if (sources.size() == 1) {
				syncListener = new SyncListener();
				registerReceiver(syncListener, new IntentFilter(SyncAdapter.ACTION_SYNC_FINISH));
			}

		} else {
			sources.remove(sourceName);

			// last source removed?
			if (sources.size() == 0) {
				unregisterReceiver(syncListener);
				syncListener = null;
			}

		}

		return START_STICKY;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();

		if (syncListener != null) {
			unregisterReceiver(syncListener);
			syncListener = null;
		}
	}


	private class SyncListener extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String sourceName = intent.getExtras().getString(SyncAdapter.EXTRA_SOURCE_NAME);
			if (sources.containsKey(sourceName)) {
				Toast.makeText(
						context, 
						"Sync finished!", 
						Toast.LENGTH_LONG)
					.show();

			} else {
				Toast.makeText(
						context, 
						"Sync finished (not monitoring)!", 
						Toast.LENGTH_LONG)
					.show();
			}
		}
	}
}
