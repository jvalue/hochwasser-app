package de.bitdroid.flooding.ods;

import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import de.bitdroid.flooding.utils.Log;


public final class OdsContentProviderTest extends ProviderTestCase2<OdsContentProvider> {

	private MockContentResolver contentResolver;

	public OdsContentProviderTest(Class<OdsContentProvider> providerClass, String authority) {
		super(providerClass, authority);
	}


	public OdsContentProviderTest() {
		super(OdsContentProvider.class, OdsSource.AUTHORITY);
	}


	@Override
	public void setUp() throws Exception {
		super.setUp();
		contentResolver = getMockContentResolver();
	}


	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		contentResolver = null;
	}


	public void testInsertGet() {
		OdsSource source = new DummySource();
		JSONObject json = new JSONObject();

		ContentValues data1 = source.saveData(json);
		data1.put(OdsSource.COLUMN_SERVER_ID, "12345");
		data1.put(OdsSource.COLUMN_SYNC_STATUS, "synced");

		ContentValues data2 = source.saveData(json);
		data2.put(OdsSource.COLUMN_SERVER_ID, "6789");
		data2.put(OdsSource.COLUMN_SYNC_STATUS, "fail");

		contentResolver.insert(
				source.toUri(),
				data1);

		contentResolver.insert(
				source.toUri(),
				data2);


		// get something out
		Cursor cursor = contentResolver.query(
				source.toUri(),
				new String[] { OdsSource.COLUMN_SERVER_ID },
				null, null, null);

		int count = cursor.getCount();
		assertTrue("Found " + count + " elements but was expecting 2", count == 2);

		cursor.close();
	}
}
