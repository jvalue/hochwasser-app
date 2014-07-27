package de.bitdroid.flooding.ods.data;

import org.json.JSONException;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;

import de.bitdroid.flooding.ods.utils.RestCall;
import de.bitdroid.flooding.ods.utils.RestException;
import de.bitdroid.flooding.utils.Log;

public final class SyncAdapter extends AbstractThreadedSyncAdapter {

	public static final String 
			ACTION_SYNC_FINISH = "de.bitdroid.flooding.ods.data.ACTION_SYNC_FINISH",
			ACTION_SYNC_ALL_FINISH = "de.bitdroid.flooding.ods.data.ACTION_SYNC_ALL_FINISH",
			EXTRA_SOURCE_NAME = "EXTRA_SOURCE_NAME",
			EXTRA_SYNC_SUCCESSFUL = "EXTRA_SYNC_SUCCESSFUL";

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

		boolean success = true;

		String sourceName = extras.getString(EXTRA_SOURCE_NAME);
		if (sourceName == null) {
			// sync all resources and sources
			for (OdsSource source : OdsSourceManager.getInstance(context).getPollingSources()) {
				success  = success && syncSource(provider, source, syncResult);
			}

		} else {
			// sync single source
			OdsSource source = OdsSource.fromString(sourceName);
			success = success && syncSource(provider, source, syncResult);
		}

		sendSyncFinishBroadcast(null, success);
		Log.debug("Sync finished");
	}


	private boolean syncSource(
			ContentProviderClient provider, 
			OdsSource source,
			SyncResult syncResult) {

		boolean success = false;

		try {
			Log.debug("Syncing " +  source.getSourceUrlPath());
			Processor processor = new Processor(provider, source);
			String retString = new RestCall.Builder(
					RestCall.RequestType.GET,
					OdsSourceManager.getInstance(context).getOdsServerName())
				.path(source.getSourceUrlPath())
				.build()
				.execute();

			processor.processGetAll(retString);
			success = true;

		} catch (RestException re1) {
			syncResult.stats.numIoExceptions++;
			Log.error(android.util.Log.getStackTraceString(re1));
		} catch (JSONException je) {
			syncResult.stats.numParseExceptions++;
			Log.error(android.util.Log.getStackTraceString(je));
		} catch (RemoteException re2) {
			syncResult.databaseError = true;
			Log.error(android.util.Log.getStackTraceString(re2));
		} catch (OperationApplicationException oae) {
			syncResult.databaseError = true;
			Log.error(android.util.Log.getStackTraceString(oae));
		}

		sendSyncFinishBroadcast(source, success);
		return success;
	}


	private void sendSyncFinishBroadcast(OdsSource source, boolean success) {
		if (source != null) {
			Intent syncFinishIntent = new Intent(ACTION_SYNC_FINISH);
			syncFinishIntent.putExtra(EXTRA_SOURCE_NAME, source.getClass().getName());
			syncFinishIntent.putExtra(EXTRA_SYNC_SUCCESSFUL,  success);
			context.sendBroadcast(syncFinishIntent);
		} else {
			Intent syncFinishIntent = new Intent(ACTION_SYNC_ALL_FINISH);
			syncFinishIntent.putExtra(EXTRA_SYNC_SUCCESSFUL,  success);
			context.sendBroadcast(syncFinishIntent);
		}
	}
}
