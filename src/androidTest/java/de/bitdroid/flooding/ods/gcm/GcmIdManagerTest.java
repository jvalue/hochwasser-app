package de.bitdroid.flooding.ods.gcm;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class GcmIdManagerTest extends AndroidTestCase {

    public void testCrud() {
        Context context = new RenamingDelegatingContext(getContext(), "test_");
        GcmIdManager manager = GcmIdManager.getInstance(context);

		assertNull(manager.getClientId());
		assertNotNull(manager.getSenderId());

		String clientId = "clientId";
		manager.updateClientId(clientId);

		assertEquals("clientId", manager.getClientId());
    }
}
