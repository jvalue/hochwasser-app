package de.bitdroid.flooding.rest;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;


public final class RestContentProviderTest extends AndroidTestCase {

	public void testBasicCrudOperations() {
		// put something in
		mContext.getContentResolver().insert(
				RestContentProvider.CONTENT_URI, 
				createContentValues("12345"));

		mContext.getContentResolver().insert(
				RestContentProvider.CONTENT_URI, 
				createContentValues("23456"));


		// get something out
		Cursor cursor = mContext.getContentResolver().query(
				RestContentProvider.CONTENT_URI,
				ODSTable.COLUMN_NAMES,
				null, null, null);

		int count = cursor.getCount();
		assertTrue("Found " + count + " elements but was expecting 2", count == 2);

		cursor.close();
	}


	private ContentValues createContentValues(String serverId) {
		ContentValues values = new ContentValues();
		values.put(ODSTable.COLUMN_SERVER_ID, serverId);
		values.put(ODSTable.COLUMN_HTTP_STATUS, "transmitted");
		values.put(ODSTable.COLUMN_SYNC_STATUS, "synced");
		values.put(ODSTable.COLUMN_JSON_DATA, "foo bar bar foo");
		return values;
	}
}
