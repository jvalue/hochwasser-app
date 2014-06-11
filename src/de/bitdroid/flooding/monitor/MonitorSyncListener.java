package de.bitdroid.flooding.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import de.bitdroid.flooding.ods.OdsSource;
import de.bitdroid.flooding.ods.SyncAdapter;


public final class MonitorSyncListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String sourceName = intent.getExtras().getString(SyncAdapter.EXTRA_SOURCE_NAME);
		OdsSource source = OdsSource.fromString(sourceName);
		if (SourceMonitor.getInstance(context).isBeingMonitored(source)) {
			Toast.makeText(
					context, 
					"Sync finished!", 
					Toast.LENGTH_LONG)
				.show();

			Intent serviceIntent = new Intent(context, CopySourceService.class);
			serviceIntent.putExtra(CopySourceService.EXTRA_SOURCE_NAME, source.toString());
			context.startService(serviceIntent);
		}
	}


}
