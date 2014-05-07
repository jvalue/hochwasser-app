package de.bitdroid.flooding.ods;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;


public final class OdsContentProviderTest extends AndroidTestCase {

	public void testBasicCrudOperations() {
		// put something in
		mContext.getContentResolver().insert(
				OdsContentProvider.CONTENT_URI, 
				createContentValues("12345"));

		mContext.getContentResolver().insert(
				OdsContentProvider.CONTENT_URI, 
				createContentValues("23456"));


		// get something out
		Cursor cursor = mContext.getContentResolver().query(
				OdsContentProvider.CONTENT_URI,
				OdsTable.COLUMN_NAMES,
				null, null, null);

		int count = cursor.getCount();
		assertTrue("Found " + count + " elements but was expecting 2", count == 2);

		cursor.close();
	}


	private ContentValues createContentValues(String serverId) {
		ContentValues values = new ContentValues();
		values.put(OdsTable.COLUMN_SERVER_ID, serverId);
		values.put(OdsTable.COLUMN_HTTP_STATUS, "transmitted");
		values.put(OdsTable.COLUMN_SYNC_STATUS, "synced");
		values.put(OdsTable.COLUMN_JSON_DATA, "foo bar bar foo");
		return values;
	}
}
