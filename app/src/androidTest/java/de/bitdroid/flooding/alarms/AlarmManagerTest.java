package de.bitdroid.flooding.alarms;

import android.test.AndroidTestCase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.util.HashMap;
import java.util.Map;

import de.bitdroid.flooding.ods.cep.CepManager;
import de.bitdroid.flooding.ods.gcm.GcmStatus;
import de.bitdroid.flooding.testUtils.PrefsRenamingDelegatingContext;

public class AlarmManagerTest extends AndroidTestCase {

	private static final ObjectMapper mapper = new ObjectMapper();

	private AlarmManager manager;
	private int addedCounter;
	private int deletedCounter;

	@Override
	public void setUp() {
		setContext(new PrefsRenamingDelegatingContext(
				getContext(),
				"AlarmManagerTest"));
		this.manager = AlarmManager.getInstance(getContext());
		this.addedCounter = 0;
		this.deletedCounter = 0;
	}


	public void testSuccess() throws Exception {
		MockWebServer server = new MockWebServer();
		server.enqueue(getRegistrationResponse("someId1"));
		server.enqueue(getRegistrationResponse("someId2"));
		server.enqueue(new MockResponse());
		server.enqueue(new MockResponse());
		server.play();
		new CepManager(getContext()).setCepServerName(server.getUrl("").toString());

		final Alarm alarm1 = new LevelAlarm("river", "station", 32, true);
		final Alarm alarm2 = new LevelAlarm("river", "station", 32, false);

		AlarmUpdateListener listener = new AlarmUpdateListener() {
			@Override
			public void onNewAlarm(Alarm alarm) {
				assertAlarm(addedCounter, alarm);
				addedCounter++;
			}

			@Override
			public void onDeletedAlarm(Alarm alarm) {
				assertAlarm(deletedCounter, alarm);
				deletedCounter++;
			}

			private void assertAlarm(int counter, Alarm alarm) {
				switch(counter) {
					case 0:
						assertEquals(alarm1, alarm);
						break;
					case 1:
						assertEquals(alarm2, alarm);
						break;
					default:
						fail();
				}
			}
		};
		manager.registerListener(listener);

		assertEquals(0, manager.getAll().size());
		assertFalse(manager.isRegistered((alarm1)));
		assertFalse(manager.isRegistered((alarm2)));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm2));

		manager.register(alarm1);
		Thread.sleep(200);

		assertEquals(1, manager.getAll().size());
		assertTrue(manager.getAll().contains(alarm1));
		assertTrue(manager.isRegistered((alarm1)));
		assertFalse(manager.isRegistered((alarm2)));
		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm2));
		assertEquals(1, addedCounter);
		assertEquals(0, deletedCounter);

		manager.register(alarm2);
		Thread.sleep(200);

		assertEquals(2, manager.getAll().size());
		assertTrue(manager.getAll().contains(alarm1));
		assertTrue(manager.getAll().contains(alarm2));
		assertTrue(manager.isRegistered((alarm1)));
		assertTrue(manager.isRegistered((alarm2)));
		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(alarm2));
		assertEquals(2, addedCounter);
		assertEquals(0, deletedCounter);

		manager.unregister(alarm1);
		Thread.sleep(200);

		assertEquals(1, manager.getAll().size());
		assertTrue(manager.getAll().contains(alarm2));
		assertFalse(manager.isRegistered((alarm1)));
		assertTrue(manager.isRegistered((alarm2)));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(alarm2));
		assertEquals(2, addedCounter);
		assertEquals(1, deletedCounter);

		manager.unregisterListener(listener);
		manager.unregister(alarm2);
		Thread.sleep(200);

		assertEquals(0, manager.getAll().size());
		assertFalse(manager.isRegistered((alarm1)));
		assertFalse(manager.isRegistered((alarm2)));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm1));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm2));
		assertEquals(2, addedCounter);
		assertEquals(1, deletedCounter);

		server.shutdown();
	}


	public void  testFailure() throws Exception {
		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setResponseCode(500));
		server.enqueue(getRegistrationResponse("someId1"));
		server.enqueue(new MockResponse());
		server.play();
		new CepManager(getContext()).setCepServerName(server.getUrl("").toString());

		final Alarm alarm = new LevelAlarm("river", "station", 32, true);

		assertEquals(0, manager.getAll().size());
		assertFalse(manager.isRegistered((alarm)));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm));

		manager.register(alarm);
		Thread.sleep(200);

		assertEquals(1, manager.getAll().size());
		assertTrue(manager.getAll().contains(alarm));
		assertTrue(manager.isRegistered((alarm)));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm));

		manager.register(alarm);
		Thread.sleep(200);

		assertEquals(1, manager.getAll().size());
		assertTrue(manager.getAll().contains(alarm));
		assertTrue(manager.isRegistered((alarm)));
		assertEquals(GcmStatus.REGISTERED, manager.getRegistrationStatus(alarm));

		manager.unregister(alarm);
		Thread.sleep(200);

		assertEquals(0, manager.getAll().size());
		assertFalse(manager.isRegistered((alarm)));
		assertEquals(GcmStatus.UNREGISTERED, manager.getRegistrationStatus(alarm));

		server.shutdown();
	}


	private MockResponse getRegistrationResponse(String clientId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("clientId", clientId);
		JsonNode json = mapper.valueToTree(map);
		return new MockResponse().setBody(((Object) json).toString());
	}
}
