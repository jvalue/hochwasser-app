package de.bitdroid.flooding.ods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;

import de.bitdroid.flooding.utils.Log;

public final class SyncAdapter extends AbstractThreadedSyncAdapter {

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


		try {
			RestCall call = new RestCall.Builder(
					RestCall.RequestType.GET, 
					"http://faui2o2f.cs.fau.de:8080")
				.path("open-data-service")
				.path("ods")
				.path("de")
				.path("pegelonline")
				.path("stations")
				.build();

			String resultString = call.execute();
			Object tmpJson = new JSONTokener(resultString).nextValue();

			if (tmpJson instanceof JSONObject) {
				insertIntoProvider((JSONObject) tmpJson, provider);

			} else if (tmpJson instanceof JSONArray) {
				JSONArray arrayJson = (JSONArray) tmpJson;
				for (int i = 0; i < arrayJson.length(); i++) {
					insertIntoProvider(arrayJson.getJSONObject(i), provider);
				}
			}


		} catch (RemoteException re) {
			syncResult.hasHardError();
			Log.error(re.getMessage());
		} catch (RestException e) {
			syncResult.hasHardError();
			Log.error(e.getMessage());
		} catch (JSONException je) {
			syncResult.hasHardError();
			Log.error(je.getMessage());
		}
	}


	private void insertIntoProvider(JSONObject object, ContentProviderClient provider) 
			throws RemoteException, JSONException {

		// query if already present
		String serverId = object.getString("_id");
		Cursor cursor = provider.query(
				OdsContentProvider.CONTENT_URI.buildUpon().appendPath(serverId).build(),
				new String[] { OdsTable.COLUMN_SERVER_ID },
				null, null, null);

		if (cursor.getCount() >= 1) {
			Log.debug("Found row, not inserting");
			return;
		}


		// insert db
		ContentValues data = new ContentValues();
		data.put(OdsTable.COLUMN_SERVER_ID, serverId);
		data.put(OdsTable.COLUMN_HTTP_STATUS, "foo");
		data.put(OdsTable.COLUMN_SYNC_STATUS, "foo");
		data.put(OdsTable.COLUMN_JSON_DATA, object.toString());

		provider.insert(OdsContentProvider.CONTENT_URI, data);
	}
}
