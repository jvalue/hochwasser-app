package de.bitdroid.ods.data;


import android.content.ContentValues;
import android.content.Context;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import de.bitdroid.ods.gcm.GcmStatus;
import de.bitdroid.testUtils.BaseAndroidTestCase;
import de.bitdroid.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.testUtils.SharedPreferencesHelper;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OdsSourceManagerTest extends BaseAndroidTestCase {

	private static final String PREFIX =  OdsSourceManagerTest.class.getSimpleName();
	private static final String serverName = "http://someServerName.com";



	private OdsSourceManager sourceManager;
	private GcmManager gcmManager;
	private SyncUtils syncUtils;
	private SyncStatusListener syncListener;

	@Override
	public void beforeClass() {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
		// some love potion for our two friends: dexmaker and mockito
		System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
	}


	@Override
	public void beforeTest() throws Exception {
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());

		Constructor<OdsSourceManager> constructor = OdsSourceManager.class.getDeclaredConstructor(
				Context.class,
				GcmManager.class,
				SyncUtils.class,
				SyncStatusListener.class);
		constructor.setAccessible(true);

		gcmManager = mock(GcmManager.class);
		syncUtils = mock(SyncUtils.class);
		syncListener = mock(SyncStatusListener.class);
		sourceManager = constructor.newInstance(getContext(), gcmManager, syncUtils, syncListener);
	}


	public void testServerName() {
		// simple check
		sourceManager.setOdsServerName(serverName);
		assertEquals(serverName, sourceManager.getOdsServerName());

		// other methods access prefs as well
		when(syncUtils.isPeriodicSyncScheduled()).thenReturn(false).thenReturn(true);
		OdsSource source1 = mock(OdsSource.class);
		OdsSource source2 = mock(OdsSource.class);

		sourceManager.startPolling(100,  false, source1, source2);
		sourceManager.stopPolling();

		assertEquals(serverName, sourceManager.getOdsServerName());
	}


	public  void testPolling() {
		OdsSource source1 = new MockSource1();
		OdsSource source2 = new MockSource2();
		long pollFrequency = 100;
		when(syncUtils.isPeriodicSyncScheduled())
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(true)
				.thenReturn(true)
				.thenReturn(false)
				.thenReturn(false);

		assertFalse(sourceManager.isPollingActive());
		assertEquals(0, sourceManager.getPollingSources().size());

		sourceManager.setOdsServerName(serverName);
		sourceManager.startPolling(pollFrequency, false, source1, source2);

		assertTrue(sourceManager.isPollingActive());
		assertEquals(2, sourceManager.getPollingSources().size());
		verify(syncUtils).startPeriodicSync(eq(serverName), any(List.class), eq(pollFrequency), eq(false));

		sourceManager.stopPolling();

		assertFalse(sourceManager.isPollingActive());
		assertEquals(0, sourceManager.getPollingSources().size());
		verify(syncUtils).stopPeriodicSync();
	}


	public void testManualSync() {
		OdsSource source = new MockSource1();

		sourceManager.setOdsServerName(serverName);
		sourceManager.startManualSync(source);

		verify(syncUtils).addAccount();
		verify(syncUtils).startManualSync(eq(serverName), notNull(OdsSource.class));
	}


	public void testPush() {
		OdsSource source = new MockSource1();

		when(gcmManager.getRegistrationStatus(source)).thenReturn(GcmStatus.UNREGISTERED);
		assertEquals(GcmStatus.UNREGISTERED, sourceManager.getPushNotificationsRegistrationStatus(source));

		sourceManager.startPushNotifications(source);
		verify(gcmManager).registerSource(source);

		when(gcmManager.getRegisteredSources()).thenReturn(new HashSet<OdsSource>(Arrays.asList(source)));
		assertEquals(1, sourceManager.getPushNotificationSources().size());

		when(gcmManager.getRegistrationStatus(source)).thenReturn(GcmStatus.REGISTERED);

		sourceManager.stopPushNotifications(source);
		verify(gcmManager).unregisterSource(source);
	}


	public static abstract class MockSource extends OdsSource {

		private final String id;

		public MockSource(String id) {
			this.id = id;
		}

		@Override
		public String getSourceId() {
			return id;
		}

		@Override
		public String getSourceUrlPath() {
			return "mockSourcePath";
		}

		@Override
		public void getSchema(Map<String, SQLiteType> schema) { }

		@Override
		protected void saveData(JSONObject json, ContentValues values) { }

	}


	public static final class MockSource1 extends MockSource {
		public MockSource1() {
			super("mockId1");
		}
	}


	public static final class MockSource2 extends MockSource {
		public MockSource2() {
			super("mockId2");
		}
	}

}
