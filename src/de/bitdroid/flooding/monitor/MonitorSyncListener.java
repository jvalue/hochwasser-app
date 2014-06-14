package de.bitdroid.flooding.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.ods.SyncAdapter;
import de.bitdroid.flooding.utils.Log;


public final class MonitorSyncListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String sourceName = intent.getStringExtra(SyncAdapter.EXTRA_SOURCE_NAME);
		OdsSource source = OdsSource.fromString(sourceName);
		boolean success = intent.getBooleanExtra(SyncAdapter.EXTRA_SYNC_SUCCESSFUL, false);

		if (SourceMonitor.getInstance(context).isBeingMonitored(source) && success) {
			Log.info("Monitor is copying db");
			Intent serviceIntent = new Intent(context, CopySourceService.class);
			serviceIntent.putExtra(CopySourceService.EXTRA_SOURCE_NAME, source.toString());
			context.startService(serviceIntent);
		}
	}

}
