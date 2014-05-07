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

		String resourceId = extras.getString(EXTRA_RESOURCE_ID);
		Processor processor = new Processor(provider);

		try {
			RestCall.Builder callBuilder = new RestCall.Builder(
					RestCall.RequestType.GET, 
					"http://faui2o2f.cs.fau.de:8080")
				.path("open-data-service");

			if (resourceId == null) {
				String resultString = callBuilder 
					.path("ods/de/pegelonline/stations")
					.build()
					.execute();

				processor.processGetAll(resultString);

			} else {
				String resultString = callBuilder 
					.path("$" + resourceId)
					.build()
					.execute();

				processor.processGetSingle(resultString);

			}

		} catch (Exception e) {
			syncResult.hasHardError();
			Log.error(android.util.Log.getStackTraceString(e));
		}
	}
}
