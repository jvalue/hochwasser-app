package de.bitdroid.ods.data;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.os.Bundle;
import android.os.RemoteException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.json.JSONException;

import java.io.IOException;

import de.bitdroid.utils.RestCall;
import de.bitdroid.utils.RestException;
import de.bitdroid.utils.Log;

public final class SyncAdapter extends AbstractThreadedSyncAdapter {

	public static final String
			ACTION_SYNC_START = "de.bitdroid.ods.data.ACTION_SYNC_START",
			ACTION_SYNC_ALL_START = "de.bitdroid.ods.data.ACTION_SYNC_ALL_START",
			ACTION_SYNC_FINISH = "de.bitdroid.ods.data.ACTION_SYNC_FINISH",
			ACTION_SYNC_ALL_FINISH = "de.bitdroid.ods.data.ACTION_SYNC_ALL_FINISH",
			EXTRA_SOURCE_JSON = "EXTRA_SOURCE_JSON",
			EXTRA_SOURCE_NAME = "EXTRA_SOURCE_NAME",
			EXTRA_ODS_URL = "EXTRA_ODS_URL",
			EXTRA_SYNC_SUCCESSFUL = "EXTRA_SYNC_SUCCESSFUL";

	private static final ObjectMapper mapper = new ObjectMapper();

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

		String odsUrl = extras.getString(EXTRA_ODS_URL);
		String jsonSources = extras.getString(EXTRA_SOURCE_JSON);
		if (odsUrl == null && jsonSources == null) {
			Log.warning("ignoring probable first sync request");
			return;
		}
		ArrayNode sourceJson = null;
		try {
			sourceJson = mapper.readValue(jsonSources, ArrayNode.class);
		} catch (IOException ioe) {
			Log.error("failed to parse sources", ioe);
		}


		Log.debug("Sync started");
		sendSyncAllStartBroadcast();

		boolean allSuccess = true;
		for (int idx = 0; idx < sourceJson.size(); idx++) {
			OdsSource source = OdsSource.fromString(sourceJson.get(idx).asText());

			sendSyncSingleStartBroadcast(source);
			boolean success = syncSource(provider, source, odsUrl, syncResult);
			allSuccess = allSuccess && success;
			sendSyncSingleFinishBroadcast(source, success);
		}

		sendSyncAllFinishBroadcast(allSuccess);
		Log.debug("Sync finished");
	}


	private boolean syncSource(
			ContentProviderClient provider, 
			OdsSource source,
			String odsUrl,
			SyncResult syncResult) {

		boolean success = false;

		try {
			Log.debug("Syncing " +  source.getSourceUrlPath());
			Processor processor = new Processor(provider, source);
			String retString = new RestCall.Builder(
					RestCall.RequestType.GET,
					odsUrl)
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

		return success;
	}


	private void sendSyncSingleStartBroadcast(OdsSource source) {
		Intent syncStartIntent = new Intent(ACTION_SYNC_START);
		syncStartIntent.putExtra(EXTRA_SOURCE_NAME, source.toString());
		context.sendBroadcast(syncStartIntent);
	}


	private void sendSyncAllStartBroadcast() {
		Intent syncStartIntent = new Intent(ACTION_SYNC_ALL_START);
		context.sendBroadcast(syncStartIntent);
	}


	private void sendSyncSingleFinishBroadcast(OdsSource source, boolean success) {
		Intent syncFinishIntent = new Intent(ACTION_SYNC_FINISH);
		syncFinishIntent.putExtra(EXTRA_SOURCE_NAME, source.toString());
		syncFinishIntent.putExtra(EXTRA_SYNC_SUCCESSFUL, success);
		context.sendBroadcast(syncFinishIntent);
	}


	private void sendSyncAllFinishBroadcast(boolean success) {
		Intent syncFinishIntent = new Intent(ACTION_SYNC_ALL_FINISH);
		syncFinishIntent.putExtra(EXTRA_SYNC_SUCCESSFUL,  success);
		context.sendBroadcast(syncFinishIntent);
	}
}
