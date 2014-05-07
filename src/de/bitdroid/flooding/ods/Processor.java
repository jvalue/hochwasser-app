package de.bitdroid.flooding.ods;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentProviderClient;
import android.content.ContentValues;
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
	public Processor(ContentProviderClient provider) {
		this.provider = provider;
	}


	public void processGetSingle(String jsonString) throws RemoteException, JSONException {
		JSONObject json = new JSONObject(jsonString);
		insertIntoProvider(json);
	}

	public void processGetAll(String jsonString) throws RemoteException, JSONException {
		JSONArray json = new JSONArray(jsonString);
		for (int i = 0; i < json.length(); i++) {
			insertIntoProvider(json.getJSONObject(i));
		}
	}

	public void prePost() {
		throw new UnsupportedOperationException();
	}
	public void postPost() {
		throw new UnsupportedOperationException();
	}


	private void insertIntoProvider(JSONObject object) 
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
		data.put(OdsTable.COLUMN_SYNC_STATUS, SyncStatus.SYNCED.toString());
		data.put(OdsTable.COLUMN_JSON_DATA, object.toString());

		provider.insert(OdsContentProvider.CONTENT_URI, data);
	}
}
