package de.bitdroid.flooding.ods.gcm;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import de.bitdroid.flooding.testUtils.SharedPreferencesHelper;

public class GcmIdManagerTest extends AndroidTestCase {

	private Context context;

	@Override
	public void setUp() {
		this.context = new RenamingDelegatingContext(getContext(), "test_");
		SharedPreferencesHelper.clearAll(context);
	}

    public void testCrud() {
        GcmIdManager manager = GcmIdManager.getInstance(context);

		assertNull(manager.getClientId());
		assertNotNull(manager.getSenderId());

		String clientId = "clientId";
		manager.updateClientId(clientId);

		assertEquals("clientId", manager.getClientId());
    }
}
