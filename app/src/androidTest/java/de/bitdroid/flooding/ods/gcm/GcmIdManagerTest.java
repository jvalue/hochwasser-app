package de.bitdroid.flooding.ods.gcm;

import android.content.Context;
import android.test.AndroidTestCase;

import java.lang.reflect.Constructor;

import de.bitdroid.flooding.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.testUtils.SharedPreferencesHelper;

public class GcmIdManagerTest extends AndroidTestCase {

	private static final String PREFIX = GcmIdManagerTest.class.getSimpleName();


    public void testCrud() throws Exception {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
		SharedPreferencesHelper.clearAll(getContext(), PREFIX);

		Constructor<GcmIdManager> constructor = GcmIdManager.class.getDeclaredConstructor(Context.class);
		constructor.setAccessible(true);
		GcmIdManager manager = constructor.newInstance(getContext());

		assertNull(manager.getClientId());
		assertNotNull(manager.getSenderId());

		String clientId = "clientId";
		manager.updateClientId(clientId);

		assertEquals("clientId", manager.getClientId());
    }
}
