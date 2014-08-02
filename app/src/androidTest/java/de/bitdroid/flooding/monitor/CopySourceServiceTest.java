package de.bitdroid.flooding.monitor;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.test.ServiceTestCase;
import android.test.mock.MockContentResolver;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.flooding.testUtils.MockContentProviderContext;
import de.bitdroid.flooding.utils.SQLiteType;

public class CopySourceServiceTest extends ServiceTestCase {


	public CopySourceServiceTest() {
		super(CopySourceService.class);
	}


	public void testCopyAndDelete() throws Exception {
		// setup content provider and data
		MockContentResolver resolver = new MockContentResolver();
		setContext(new MockContentProviderContext(
				getContext(),
				resolver,
				CopySourceServiceTest.class.getSimpleName()));

		ListContentProvider provider = new ListContentProvider();
		provider.addCursor(MockOdsSource.getSampelCursor());
		provider.addCursor(MockOdsSource.getSampelCursor());
		provider.addCursor(MockOdsSource.getSampelCursor());
		provider.addCursor(MockOdsSource.getSampelCursor());
		resolver.addProvider(OdsSource.AUTHORITY, provider);

		OdsSource source = new MockOdsSource();
		SourceMonitor monitor = SourceMonitor.getInstance(getContext());

		// setup monitor frequency
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
		editor.putString(getContext().getString(R.string.prefs_ods_monitor_days_key), String.valueOf(1));
		editor.putString(getContext().getString(R.string.prefs_ods_monitor_interval_key), String.valueOf(11.5f));
		editor.commit();

		// start first copying
		monitor.startMonitoring(source);
		assertTrue(monitor.isBeingMonitored(source));

		Intent intent = new Intent(getContext(), CopySourceService.class);
		intent.putExtra(CopySourceService.EXTRA_SOURCE_NAME, source.toString());

		assertEquals(0, monitor.getAvailableTimestamps(source).size());

		startService(intent);
		Thread.sleep(200);
		assertEquals(1, monitor.getAvailableTimestamps(source).size());

		startService(intent);
		Thread.sleep(200);
		assertEquals(2, monitor.getAvailableTimestamps(source).size());

		// old values should have been deleted
		startService(intent);
		Thread.sleep(200);
		assertEquals(2, monitor.getAvailableTimestamps(source).size());

		startService(intent);
		Thread.sleep(200);
		assertEquals(2, monitor.getAvailableTimestamps(source).size());

		monitor.stopMonitoring(source);
		assertFalse(monitor.isBeingMonitored(source));
	}


	private static class ListContentProvider extends android.test.mock.MockContentProvider {

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

			if (cursors.size() == 0) return null;
			return cursors.remove(0);
		}
	}

	public static class MockOdsSource extends OdsSource {

		private static final String COLUMN = "value";

		@Override
		public String getSourceUrlPath() {
			return "someUrl";
		}

		@Override
		public String getSourceId() {
			return "someId";
		}

		@Override
		public Map<String, SQLiteType> getSchema() {
			Map<String, SQLiteType> schema = new HashMap<String, SQLiteType>();
			schema.put(COLUMN, SQLiteType.TEXT);
			return schema;
		}

		@Override
		public ContentValues saveData(JSONObject json) {
			throw new UnsupportedOperationException("stub");
		}

		public static Cursor getSampelCursor() {
			MatrixCursor cursor = new MatrixCursor(new String[] {
					OdsSource.COLUMN_SERVER_ID,
					COLUMN });
			for (int i = 0; i < 10; i++) {
				String randomValue = String.valueOf(Math.random());
				cursor.addRow(new String[] { randomValue, randomValue });
			}
			return cursor;
		}
	}

}
