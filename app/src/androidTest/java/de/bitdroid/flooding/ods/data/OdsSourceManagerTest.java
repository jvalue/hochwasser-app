package de.bitdroid.flooding.ods.data;


import android.content.Context;

import java.lang.reflect.Constructor;

import de.bitdroid.flooding.testUtils.BaseAndroidTestCase;
import de.bitdroid.flooding.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.testUtils.SharedPreferencesHelper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OdsSourceManagerTest extends BaseAndroidTestCase {

	private static final String PREFIX =  OdsSourceManagerTest.class.getSimpleName();


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
		String serverName = "http://someServerName.com";

		// simple check
		sourceManager.setOdsServerName(serverName);
		assertEquals(serverName, sourceManager.getOdsServerName());

		// other methods access prefs as well
		when(syncUtils.isPeriodicSyncScheduled()).thenReturn(false).thenReturn(true);
		OdsSource source1 = mock(OdsSource.class);
		OdsSource source2 = mock(OdsSource.class);

		sourceManager.startPolling(100,  source1, source2);
		sourceManager.stopPolling();

		assertEquals(serverName, sourceManager.getOdsServerName());
	}

}
