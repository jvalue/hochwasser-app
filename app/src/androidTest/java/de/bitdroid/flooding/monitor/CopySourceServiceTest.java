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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.ods.data.OdsSource;
import de.bitdroid.flooding.testUtils.ContentProviderContext;
import de.bitdroid.flooding.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.testUtils.SharedPreferencesHelper;
import de.bitdroid.flooding.ods.data.SQLiteType;

public class CopySourceServiceTest extends ServiceTestCase {

	private static final String PREFIX = CopySourceServiceTest.class.getSimpleName();


	public CopySourceServiceTest() {
		super(CopySourceService.class);
	}


	public void testCopyAndDelete() throws Exception {
		// setup content provider and data
		MockContentResolver resolver = new MockContentResolver();
		setContext(new PrefsRenamingDelegatingContext(new ContentProviderContext(getContext(), resolver), PREFIX));

		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());

		ListContentProvider provider = new ListContentProvider();
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

		long timestamp = System.currentTimeMillis();
		provider.addCursor(MockOdsSource.getSampleCursor(timestamp));
		startService(intent);
		Thread.sleep(300);
		assertEquals(1, monitor.getAvailableTimestamps(source).size());

		// no copying since same timestamp
		provider.addCursor(MockOdsSource.getSampleCursor(timestamp));
		startService(intent);
		Thread.sleep(300);
		assertEquals(1, monitor.getAvailableTimestamps(source).size());

		provider.addCursor(MockOdsSource.getSampleCursor(System.currentTimeMillis()));
		startService(intent);
		Thread.sleep(300);
		assertEquals(2, monitor.getAvailableTimestamps(source).size());

		// old values should have been deleted
		provider.addCursor(MockOdsSource.getSampleCursor(System.currentTimeMillis()));
		startService(intent);
		Thread.sleep(300);
		assertEquals(2, monitor.getAvailableTimestamps(source).size());

		provider.addCursor(MockOdsSource.getSampleCursor(System.currentTimeMillis()));
		startService(intent);
		Thread.sleep(300);
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
		protected void getSchema(Map<String, SQLiteType> schema) {
			schema.put(COLUMN, SQLiteType.TEXT);
		}

		@Override
		protected void saveData(JSONObject json, ContentValues values) {
			throw new UnsupportedOperationException("stub");
		}

		public static Cursor getSampleCursor(long timestamp) {
			MatrixCursor cursor = new MatrixCursor(new String[] {
					OdsSource.COLUMN_SERVER_ID,
					OdsSource.COLUMN_TIMESTAMP,
					COLUMN });
			for (int i = 0; i < 10; i++) {
				String randomValue = String.valueOf(Math.random());
				cursor.addRow(new String[] { randomValue, String.valueOf(timestamp), randomValue});
			}
			return cursor;
		}
	}

}
