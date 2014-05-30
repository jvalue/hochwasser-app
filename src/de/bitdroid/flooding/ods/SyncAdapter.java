package de.bitdroid.flooding.ods;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import de.bitdroid.flooding.utils.Log;

public final class SyncAdapter extends AbstractThreadedSyncAdapter {

	public static final String EXTRA_SOURCE_NAME = "sourceName";

	private final Context context;


	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		this.context = context;
	}

	public SyncAdapter(
			Context context,
			boolean autoInitialize,
			boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		this.context = context;
	}

	@Override
	public void onPerformSync(
			Account account,
			Bundle extras,
			String authority,
			ContentProviderClient provider,
			SyncResult syncResult) {

		Log.debug("Sync started");

		try {

			String sourceName = extras.getString(EXTRA_SOURCE_NAME);
			if (sourceName == null) {
				// sync all resources and sources
				for (OdsSource source : OdsSourceManager.getInstance(context).getSources()) {
					syncSource(provider, source);
				}
			} else {
				// sync single source
				syncSource(provider, OdsSource.fromClassName(sourceName));
			}


			Log.debug("Sync finished");

		} catch (Exception e) {
			syncResult.hasHardError();
			Log.error(android.util.Log.getStackTraceString(e));
		}
	}


	private void syncSource(
			ContentProviderClient provider, 
			OdsSource source) throws Exception {

		Log.debug("Syncing " +  source.getSourceUrlPath());
		Processor processor = new Processor(provider, source);
		String retString = new RestCall.Builder(
				RestCall.RequestType.GET,
				OdsSourceManager.getInstance(context).getOdsServerName())
			.path(source.getSourceUrlPath())
			.build()
			.execute();

		processor.processGetAll(retString);
	}
}
