package de.bitdroid.flooding.ods.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import de.bitdroid.flooding.utils.Log;


/* What is the processor all about?
 *
 * Managing and upading the sync status flags for all resources
 *
 * Why? Such that clients know what status their data is in, such
 * as synced or currently being synced.
 */
final class Processor {

	private final ContentProviderClient provider;
	private final OdsSource source;

	public Processor(ContentProviderClient provider, OdsSource source) {
		this.provider = provider;
		this.source = source;
	}


	public void processGetAll(String jsonString) 
			throws RemoteException, JSONException, OperationApplicationException {

		// delete all entries
		int deleteCount = provider.delete(source.toUri(), null, null);
		Log.debug("Removed " + deleteCount + " rows");

		// insert new entries
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		JSONArray json = new JSONArray(jsonString);
		Log.debug("Fetched " + json.length() + " items from server");
		for (int i = 0; i < json.length(); i++) {
			ContentProviderOperation operation = insertIntoProvider(json.getJSONObject(i));
			if (operation != null) operations.add(operation);
			if (operations.size() >= 100) {
				provider.applyBatch(operations);
				operations.clear();
			}
		}

		if (operations.size() > 0) provider.applyBatch(operations);
	}

	public void prePost() {
		throw new UnsupportedOperationException();
	}
	public void postPost() {
		throw new UnsupportedOperationException();
	}


	private ContentProviderOperation insertIntoProvider(JSONObject object) 
			throws RemoteException, JSONException {

		String serverId = object.optString("_id", null);
		ContentValues data = source.saveData(object);
		data.put(OdsSource.COLUMN_SERVER_ID, serverId);
		data.put(OdsSource.COLUMN_SYNC_STATUS, SyncStatus.SYNCED.toString());

		// query if already present
		Cursor cursor = null;
		try {
			cursor = provider.query(
					source.toUri(),
					new String[] { OdsSource.COLUMN_ID, OdsSource.COLUMN_SERVER_ID },
					OdsSource.COLUMN_SERVER_ID + " = ?",
					new String[] { serverId },
					null);

			// update data 
			if (cursor.getCount() >= 1) {
				cursor.moveToFirst();
				String id = cursor.getString(0);
				data.put(OdsSource.COLUMN_ID, id);

				if (cursor.getCount() > 1) {
					Log.warning("Found multiple objects in db with server id " + serverId);
				}

				return ContentProviderOperation
					.newUpdate(source.toUri())
					.withValues(data)
					.build();

			// insert new data
			} else {
				return ContentProviderOperation
					.newInsert(source.toUri())
					.withValues(data)
					.build();
			}
		} finally {
			if (cursor != null) cursor.close();
		}
	}
}
