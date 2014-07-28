package de.bitdroid.flooding.ods.data;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;


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
		String id = "1234";
		String statusOk = "ok", statusFail = "fail";


		// test insert & read
		ContentValues data1 = getValues(id, statusOk);
		ContentValues data2 = getValues("6789", statusFail);

		contentResolver.insert(source.toUri(), data1); 
		contentResolver.insert(source.toUri(), data2); 

		Cursor cursor = contentResolver.query(
				source.toUri(),
				new String[] { OdsSource.COLUMN_SERVER_ID, OdsSource.COLUMN_SYNC_STATUS },
				null, null, null);

		int count = cursor.getCount();
		assertTrue("Found " + count + " elements but was expecting 2", count == 2);

		List<String> requiredStatus = new LinkedList<String>();
		requiredStatus.add(statusOk);
		requiredStatus.add(statusFail);

		cursor.moveToFirst();
		do {
			String status = cursor.getString(1);
			assertTrue("Failed to find status " + status, requiredStatus.remove(status));
		} while (cursor.moveToNext());
		cursor.close();


		// test update
		data1 = getValues(id, statusFail);
		contentResolver.update(source.toUri(), data1, null, null); 

		cursor = contentResolver.query(
				source.toUri(),
				new String[] { OdsSource.COLUMN_SERVER_ID, OdsSource.COLUMN_SYNC_STATUS },
				null, null, null);

		requiredStatus.add(statusFail);
		requiredStatus.add(statusFail);

		cursor.moveToFirst();
		do {
			String status = cursor.getString(1);
			assertTrue("Failed to find status " + status, requiredStatus.remove(status));
		} while (cursor.moveToNext());
		cursor.close();


		// test delete
		int deleteCount = contentResolver.delete(source.toUri(), null, null);
		assertTrue("Deleted " + deleteCount + ", expected 2", deleteCount == 2);
		cursor.close();
	}




	private ContentValues getValues(String id, String syncStatus) {
		JSONObject json = new JSONObject();
		ContentValues data = source.saveData(json);
		data.put(OdsSource.COLUMN_SERVER_ID, id);
		data.put(OdsSource.COLUMN_SYNC_STATUS, syncStatus);
		return data;
	}

}
