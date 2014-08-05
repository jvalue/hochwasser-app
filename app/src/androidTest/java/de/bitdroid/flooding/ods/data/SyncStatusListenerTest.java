package de.bitdroid.flooding.ods.data;

import android.content.ContentValues;
import android.content.Intent;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Map;

import de.bitdroid.flooding.testUtils.BaseAndroidTestCase;
import de.bitdroid.flooding.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.testUtils.SharedPreferencesHelper;
import de.bitdroid.flooding.utils.SQLiteType;

public class SyncStatusListenerTest extends BaseAndroidTestCase {

	private static final String PREFIX = SyncStatusListenerTest.class.getSimpleName();

	@Override
	public void beforeClass() {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
	}


	@Override
	public void beforeTest() {
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());
	}


	public void testSyncTimestamps() throws Exception {
		OdsSource source = new DummySource();
		SyncStatusListener listener = new SyncStatusListener(getContext());

		assertNull(listener.getLastSync(source));
		assertNull(listener.getLastSuccessfulSync(source));
		assertNull(listener.getLastFailedSync(source));

		Intent successIntent = new Intent(SyncAdapter.ACTION_SYNC_FINISH);
		successIntent.putExtra(SyncAdapter.EXTRA_SOURCE_NAME, source.toString());
		successIntent.putExtra(SyncAdapter.EXTRA_SYNC_SUCCESSFUL, true);
		new SyncStatusListener().onReceive(getContext(), successIntent);

		Thread.sleep(100);

		Calendar lastSuccess = listener.getLastSuccessfulSync(source);
		assertNotNull(lastSuccess);
		assertEquals(lastSuccess, listener.getLastSync(source));
		assertNull(listener.getLastFailedSync(source));

		Intent failIntent = new Intent(SyncAdapter.ACTION_SYNC_FINISH);
		failIntent.putExtra(SyncAdapter.EXTRA_SOURCE_NAME, source.toString());
		failIntent.putExtra(SyncAdapter.EXTRA_SYNC_SUCCESSFUL, false);
		new SyncStatusListener().onReceive(getContext(), failIntent);

		Thread.sleep(100);

		Calendar lastFailure = listener.getLastFailedSync(source);
		assertNotNull(lastFailure);
		assertEquals(lastFailure, listener.getLastSync(source));
		assertEquals( lastSuccess, listener.getLastSuccessfulSync(source));
	}


	public void testSyncRunning() throws  Exception {
		SyncStatusListener listener = new SyncStatusListener(getContext());
		assertFalse(listener.isSyncRunning());

		Intent startIntent = new Intent(SyncAdapter.ACTION_SYNC_ALL_START);
		new SyncStatusListener().onReceive(getContext(), startIntent);

		Thread.sleep(100);

		assertTrue(listener.isSyncRunning());

		Intent stopIntent = new Intent(SyncAdapter.ACTION_SYNC_ALL_FINISH);
		new SyncStatusListener().onReceive(getContext(), stopIntent);

		Thread.sleep(100);

		assertFalse(listener.isSyncRunning());
	}



	public static class DummySource extends OdsSource {

		@Override
		public String getSourceUrlPath() {
			return "dummySourceUrl";
		}

		@Override
		protected void getSchema(Map<String, SQLiteType> schema) { }

		@Override
		public String getSourceId() {
			return "dummySourceId";
		}

		@Override
		protected void saveData(JSONObject jsonObject, ContentValues values) { }
	}

}
