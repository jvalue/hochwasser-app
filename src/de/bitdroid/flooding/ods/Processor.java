package de.bitdroid.flooding.ods;

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


	public void processGetSingle(String jsonString) 
			throws RemoteException, JSONException, OperationApplicationException {

		JSONObject json = new JSONObject(jsonString);
		ContentProviderOperation operation = insertIntoProvider(json);
		if (operation != null) {
			ArrayList<ContentProviderOperation> operations 
				= new ArrayList<ContentProviderOperation>();
			operations.add(operation);
			provider.applyBatch(operations);
		}
	}

	public void processGetAll(String jsonString) 
			throws RemoteException, JSONException, OperationApplicationException {

		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		JSONArray json = new JSONArray(jsonString);
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

		// query if already present
		String serverId = object.optString("_id");
		if (serverId != null) {
			Cursor cursor = null;
			try {
				cursor = provider.query(
						source.toUri(),
						new String[] { OdsSource.COLUMN_SERVER_ID },
						OdsSource.COLUMN_SERVER_ID + " = ?",
						new String[] { serverId },
						null);

				if (cursor != null && cursor.getCount() >= 1) return null;

			} finally {
				if (cursor != null) cursor.close();
			}
		}


		// insert db
		ContentValues data = source.saveData(object);
		data.put(OdsSource.COLUMN_SERVER_ID, serverId);
		data.put(OdsSource.COLUMN_SYNC_STATUS, SyncStatus.SYNCED.toString());

		ContentProviderOperation.Builder builder 
			= ContentProviderOperation.newInsert(source.toUri());
		builder.withValues(data);
		return builder.build();
	}
}
