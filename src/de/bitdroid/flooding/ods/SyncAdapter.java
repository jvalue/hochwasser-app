package de.bitdroid.flooding.ods;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import de.bitdroid.flooding.utils.Log;

public final class SyncAdapter extends AbstractThreadedSyncAdapter {

	public static String EXTRA_RESOURCE_ID = "resourceId";

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

		String resourceId = extras.getString(EXTRA_RESOURCE_ID);

		try {

			// sync all resources and sources
			if (resourceId == null) {

				Log.debug("Syncing all sources");

				OdsSourceManager sourceManager = OdsSourceManager.getInstance(context);

				for (OdsSource source : sourceManager.getSources()) {
					Log.debug("... " +  source.getSourceUrlPath());
					Processor processor = new Processor(provider, source);
					String retString = new RestCall.Builder(
							RestCall.RequestType.GET,
							sourceManager.getOdsServerName())
						.path(source.getSourceUrlPath())
						.build()
						.execute();

					processor.processGetAll(retString);
				}

			// sync one resource only
			} else {

				Log.debug("Syncing single resource");
				/*
				String resultString = new RestCall.Builder(
						RestCall.RequestType.GET, 
						"http://faui2o2f.cs.fau.de:8080")
					.path("open-data-service")
					.path("$" + resourceId)
					.build()
					.execute();

				processor.processGetSingle(resultString);
				*/
				throw new UnsupportedOperationException("under construction");
			}

			Log.debug("Sync finished");

		} catch (Exception e) {
			syncResult.hasHardError();
			Log.error(android.util.Log.getStackTraceString(e));
		}
	}
}
