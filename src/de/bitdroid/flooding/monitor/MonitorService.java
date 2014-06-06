package de.bitdroid.flooding.monitor;

import java.util.Set;

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


	private BroadcastReceiver syncListener;


	@Override
	public IBinder onBind(Intent arg) {
		return null;
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Set<OdsSource> sources = SourceMonitor.getInstance(this).getMonitoredSources();

		if (sources.size() > 0) {
			if (syncListener == null) {
				syncListener = new SyncListener();
				registerReceiver(syncListener, new IntentFilter(SyncAdapter.ACTION_SYNC_FINISH));
			}
		} else {
			if (syncListener != null) {
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
			OdsSource source = OdsSource.fromClassName(sourceName);
			if (SourceMonitor.getInstance(MonitorService.this).isBeingMonitored(source)) {
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
