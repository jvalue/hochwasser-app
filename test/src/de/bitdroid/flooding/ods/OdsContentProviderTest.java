package de.bitdroid.flooding.ods;

import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;


public final class OdsContentProviderTest extends AndroidTestCase {

	public void testInsertGet() {
		OdsSource source = new DummySource();
		JSONObject json = new JSONObject();

		ContentValues data1 = source.saveData(json);
		data1.put(OdsSource.COLUMN_SERVER_ID, "12345");
		data1.put(OdsSource.COLUMN_SYNC_STATUS, "synced");

		ContentValues data2 = source.saveData(json);
		data2.put(OdsSource.COLUMN_SERVER_ID, "6789");
		data2.put(OdsSource.COLUMN_SYNC_STATUS, "fail");

		mContext.getContentResolver().insert(
				source.toUri(),
				data1);

		mContext.getContentResolver().insert(
				source.toUri(),
				data2);


		// get something out
		Cursor cursor = mContext.getContentResolver().query(
				source.toUri(),
				new String[] { OdsSource.COLUMN_SERVER_ID },
				null, null, null);

		int count = cursor.getCount();
		assertTrue("Found " + count + " elements but was expecting 2", count == 2);

		cursor.close();
	}
}
