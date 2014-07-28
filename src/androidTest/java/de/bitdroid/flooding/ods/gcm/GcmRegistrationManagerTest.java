package de.bitdroid.flooding.ods.gcm;

import android.content.Context;
import android.test.AndroidTestCase;

public class GcmRegistrationManagerTest extends AndroidTestCase {

    private static final String PREFS_NAME = "test";

    private Context context;

    @Override
    public void setUp() {
        this.context = getContext();
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }


    @Override
    public void tearDown() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }


    public void testCurd() {
        GcmRegistrationManager manager = new GcmRegistrationManager(context, PREFS_NAME);

        assertEquals(0, manager.getAllObjects().size());
        assertEquals(0, manager.getAllObjects(GcmStatus.PENDING_REGISTRATION).size());

        String object1 = "object1";
        manager.update(object1, null, GcmStatus.PENDING_REGISTRATION);

        assertEquals(1, manager.getAllObjects().size());
        assertTrue(manager.getAllObjects().contains(object1));
        assertEquals(1, manager.getAllObjects(GcmStatus.PENDING_REGISTRATION).size());
        assertEquals(0, manager.getAllObjects(GcmStatus.PENDING_UNREGISTRATION).size());
        assertEquals(0, manager.getAllObjects(GcmStatus.REGISTERED).size());
        assertEquals(0, manager.getAllObjects(GcmStatus.UNREGISTERED).size());
        assertEquals(null, manager.getClientIdForObjectId(object1));
        assertEquals(GcmStatus.PENDING_REGISTRATION, manager.getStatusForObjectId(object1));

        String clientId1 = "clientId1";
        manager.update(object1, clientId1, GcmStatus.REGISTERED);

        assertEquals(1, manager.getAllObjects().size());
        assertTrue(manager.getAllObjects().contains(object1));
        assertEquals(0, manager.getAllObjects(GcmStatus.PENDING_REGISTRATION).size());
        assertEquals(0, manager.getAllObjects(GcmStatus.PENDING_UNREGISTRATION).size());
        assertEquals(1, manager.getAllObjects(GcmStatus.REGISTERED).size());
        assertEquals(0, manager.getAllObjects(GcmStatus.UNREGISTERED).size());
        assertEquals(clientId1, manager.getClientIdForObjectId(object1));
        assertEquals(GcmStatus.REGISTERED, manager.getStatusForObjectId(object1));
        assertEquals(object1, manager.getObjectIdForClientId((clientId1)));


        String object2 = "object2";
        String clientId2 = "clientId2";
        assertEquals(GcmStatus.UNREGISTERED, manager.getStatusForObjectId(object2));
        manager.update(object2, clientId2, GcmStatus.PENDING_UNREGISTRATION);

        assertEquals(2, manager.getAllObjects().size());
        assertTrue(manager.getAllObjects().contains(object2));
        assertEquals(0, manager.getAllObjects(GcmStatus.PENDING_REGISTRATION).size());
        assertEquals(1, manager.getAllObjects(GcmStatus.PENDING_UNREGISTRATION).size());
        assertEquals(1, manager.getAllObjects(GcmStatus.REGISTERED).size());
        assertEquals(0, manager.getAllObjects(GcmStatus.UNREGISTERED).size());
        assertEquals(clientId2, manager.getClientIdForObjectId(object2));
        assertEquals(GcmStatus.PENDING_UNREGISTRATION, manager.getStatusForObjectId(object2));
        assertEquals(object2, manager.getObjectIdForClientId((clientId2)));
    }

}
