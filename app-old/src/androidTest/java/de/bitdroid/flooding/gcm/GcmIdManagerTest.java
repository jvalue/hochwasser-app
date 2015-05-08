package de.bitdroid.flooding.gcm;

import android.test.AndroidTestCase;

import de.bitdroid.flooding.utils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.utils.SharedPreferencesHelper;

public class GcmIdManagerTest extends AndroidTestCase {

	private static final String PREFIX = GcmIdManagerTest.class.getSimpleName();


    public void testCrud() throws Exception {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());

		GcmIdManager manager = new GcmIdManager(getContext());

		assertNull(manager.getClientId());
		assertNotNull(manager.getSenderId());

		String clientId = "clientId";
		manager.updateClientId(clientId);
		assertEquals(clientId, manager.getClientId());
    }
}
