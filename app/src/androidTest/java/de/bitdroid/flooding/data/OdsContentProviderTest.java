package de.bitdroid.flooding.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;


public final class OdsContentProviderTest extends ProviderTestCase2<OdsContentProvider> {

	private final OdsSource source = new DummySource();
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


	public void testCRUD() {
		String id1 = "1234";
		String id2 = "6789";

		// test insert & read
		long timestamp = System.currentTimeMillis();
		ContentValues data1 = getValues(id1, timestamp);
		ContentValues data2 = getValues(id2, timestamp);

		contentResolver.insert(source.toUri(), data1); 
		contentResolver.insert(source.toUri(), data2); 

		Cursor cursor = contentResolver.query(
				source.toUri(),
				new String[] { OdsSource.COLUMN_SERVER_ID , OdsSource.COLUMN_TIMESTAMP },
				null, null, null);

		assertEquals(2, cursor.getCount());

		List<String> ids = new LinkedList<String>();
		ids.add(id1);
		ids.add(id2);

		cursor.moveToFirst();
		do {
			String id = cursor.getString(0);
			long time = cursor.getLong(1);

			assertTrue(ids.contains(id));
			ids.remove(id);
			assertEquals(timestamp, time);

		} while (cursor.moveToNext());
		cursor.close();


		// test update
		long newTimestamp = System.currentTimeMillis();
		data1 = getValues(id1, newTimestamp);
		contentResolver.update(source.toUri(), data1, null, null);

		cursor = contentResolver.query(
				source.toUri(),
				new String[] { OdsSource.COLUMN_SERVER_ID, OdsSource.COLUMN_TIMESTAMP },
				null, null, null);

		boolean foundNewTimestamp = false;
		cursor.moveToFirst();
		do {
			if (cursor.getString(0).equals(id1)) {
				assertEquals(newTimestamp, cursor.getLong(1));
				foundNewTimestamp = true;
			}
		} while (cursor.moveToNext());
		cursor.close();

		assertTrue(foundNewTimestamp);

		// test delete
		int deleteCount = contentResolver.delete(source.toUri(), null, null);
		assertEquals(2, deleteCount);
		cursor.close();
	}


	private ContentValues getValues(String id, long timestamp) {
		JSONObject json = new JSONObject();
		ContentValues data = source.saveData(json, System.currentTimeMillis());
		data.put(OdsSource.COLUMN_SERVER_ID, id);
		data.put(OdsSource.COLUMN_TIMESTAMP, timestamp);
		return data;
	}

}
