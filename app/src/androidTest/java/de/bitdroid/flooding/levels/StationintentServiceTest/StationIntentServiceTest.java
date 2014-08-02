package de.bitdroid.flooding.levels.StationintentServiceTest;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.test.ServiceTestCase;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.levels.StationIntentService;
import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.flooding.ods.data.OdsSourceManager;
import de.bitdroid.flooding.pegelonline.PegelOnlineSource;
import de.bitdroid.flooding.testUtils.MockContentProviderContext;
import de.bitdroid.flooding.utils.Log;

public class StationIntentServiceTest extends ServiceTestCase {


	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
	private static final String stationName = "someStation";

	private static int updateCount, insertCount, queryCount = 0;


	public StationIntentServiceTest() {
		super(StationIntentService.class);
	}


	@Override
	public void setUp() throws Exception {
		super.setUp();
		updateCount = 0;
		insertCount = 0;
		queryCount = 0;
	}


	public void testSyncStation() throws Exception {
		int maxAge = getContext().getResources().getInteger(R.integer.station_max_age_in_ms);
		Log.info("maxAge is " + maxAge);

		// setup context
		MockContentResolver resolver = new MockContentResolver();
		setContext(new MockContentProviderContext(
				getContext(),
				resolver,
				StationIntentServiceTest.class.getSimpleName()));

		ListContentProvider provider = new ListContentProvider();
		resolver.addProvider(PegelOnlineSource.INSTANCE.AUTHORITY, provider);

		// setup server
		String eitze1Json = getJsonResource("eitze1.json");
		String eitze2Json = getJsonResource("eitze2.json");
		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setBody(eitze1Json));
		server.enqueue(new MockResponse().setBody(eitze2Json));
		server.play();
		OdsSourceManager.getInstance(getContext()).setOdsServerName(server.getUrl("").toString());

		// service setup
		Intent intent = new Intent(getContext(), StationIntentService.class);
		intent.putExtra(StationIntentService.EXTRA_STATION_NAME, stationName);

		// test empty provider
		provider.addCursor(createCursor(null));
		startService(intent);
		Thread.sleep(200);

		assertEquals(1, queryCount);
		assertEquals(1, insertCount);
		assertEquals(0, updateCount);
		assertTrue(server.takeRequest().getPath().contains(stationName));

		// test full + old provider
		long timestamp = System.currentTimeMillis();
		timestamp -= maxAge * 10;
		Date oldDate = new Date(timestamp);
		provider.addCursor(createCursor(oldDate));
		startService(intent);
		Thread.sleep(200);

		assertEquals(2, queryCount);
		assertEquals(1, insertCount);
		assertEquals(1, updateCount);
		assertTrue(server.takeRequest().getPath().contains(stationName));

		// test full + new provider
		timestamp = System.currentTimeMillis();
		timestamp += maxAge * 10;
		Date newDate = new Date(timestamp);
		provider.addCursor(createCursor(newDate));
		startService(intent);
		Thread.sleep(200);
		assertEquals(3, queryCount);
		assertEquals(1, insertCount);
		assertEquals(1, updateCount);

		server.shutdown();
	}


	private String getJsonResource(String fileName) throws Exception {
		Context testContext =  getContext().createPackageContext(
				"de.bitdroid.flooding.test",
				Context.CONTEXT_IGNORE_SECURITY);

		InputStream input = testContext.getAssets().open(fileName);
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) builder.append(line);
		return builder.toString();
	}


	private Cursor createCursor(Date date) {
		MatrixCursor cursor = new MatrixCursor(new String[] {
				PegelOnlineSource.COLUMN_LEVEL_TIMESTAMP,
				OdsSource.COLUMN_ID	});
		if (date != null) cursor.addRow(new Object[] { dateFormat.format(date), String.valueOf(0) });
		return cursor;
	}


	private static class ListContentProvider extends MockContentProvider {

		private final List<Cursor> cursors = new LinkedList<Cursor>();

		public void addCursor(Cursor cursor) {
			cursors.add(cursor);
		}

		@Override
		public Cursor query(
				Uri uri,
				String[] projection,
				String selection,
				String[] selectionArgs,
				String sortOrder) {

			if (cursors.size() == 0) fail("no more cursors left");
			queryCount++;
			return cursors.remove(0);
		}


		@Override
		public Uri insert(Uri uri, ContentValues values) {
			assertNotNull(uri);
			assertNotNull(values);
			insertCount++;
			return uri;
		}


		@Override
		public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
			assertNotNull(values);
			updateCount++;
			return 1;
		}


	}

}
