package de.bitdroid.flooding.ods.data;

import android.content.ContentValues;
import android.content.Intent;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.bitdroid.flooding.utils.SQLiteType;

public class SyncStatusListenerTest extends AndroidTestCase {

	@Override
	public void setUp() {
		setContext(new RenamingDelegatingContext(
				getContext(),
				SyncStatusListenerTest.class.getSimpleName()));
	}


	public void testSyncTimestamps() throws Exception {
		OdsSource source = new DummySource();

		assertNull(SyncStatusListener.getLastSync(getContext(), source));
		assertNull(SyncStatusListener.getLastSuccessfulSync(getContext(), source));
		assertNull(SyncStatusListener.getLastFailedSync(getContext(), source));

		Intent successIntent = new Intent(SyncAdapter.ACTION_SYNC_FINISH);
		successIntent.putExtra(SyncAdapter.EXTRA_SOURCE_NAME, source.toString());
		successIntent.putExtra(SyncAdapter.EXTRA_SYNC_SUCCESSFUL, true);
		getContext().sendBroadcast(successIntent);

		Thread.sleep(100);

		Calendar lastSuccess = SyncStatusListener.getLastSuccessfulSync(getContext(), source);
		assertNotNull(lastSuccess);
		assertEquals(lastSuccess, SyncStatusListener.getLastSync(getContext(), source));
		assertNull(SyncStatusListener.getLastFailedSync(getContext(), source));

		Intent failIntent = new Intent(SyncAdapter.ACTION_SYNC_FINISH);
		failIntent.putExtra(SyncAdapter.EXTRA_SOURCE_NAME, source.toString());
		failIntent.putExtra(SyncAdapter.EXTRA_SYNC_SUCCESSFUL, false);
		getContext().sendBroadcast(failIntent);

		Thread.sleep(100);

		Calendar lastFailure = SyncStatusListener.getLastFailedSync(getContext(), source);
		assertNotNull(lastFailure);
		assertEquals(lastFailure, SyncStatusListener.getLastSync(getContext(), source));
		assertEquals( lastSuccess, SyncStatusListener.getLastSuccessfulSync(getContext(), source));
	}


	public void testSyncRunning() throws  Exception {
		assertFalse(SyncStatusListener.isSyncRunning(getContext()));

		Intent startIntent = new Intent(SyncAdapter.ACTION_SYNC_START);
		getContext().sendBroadcast(startIntent);

		Thread.sleep(100);

		assertTrue(SyncStatusListener.isSyncRunning(getContext()));

		Intent stopIntent = new Intent(SyncAdapter.ACTION_SYNC_ALL_FINISH);
		getContext().sendBroadcast(stopIntent);

		Thread.sleep(100);

		assertFalse(SyncStatusListener.isSyncRunning(getContext()));
	}



	public static class DummySource extends OdsSource {

		@Override
		public String getSourceUrlPath() {
			return "dummySourceUrl";
		}

		@Override
		public Map<String, SQLiteType> getSchema() {
			return new HashMap<String, SQLiteType>();
		}

		@Override
		public String getSourceId() {
			return "dummySourceId";
		}

		@Override
		public ContentValues saveData(JSONObject jsonObject) {
			return new ContentValues();
		}
	}

}
