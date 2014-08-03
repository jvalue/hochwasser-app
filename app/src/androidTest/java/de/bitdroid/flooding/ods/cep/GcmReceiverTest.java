package de.bitdroid.flooding.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import de.bitdroid.flooding.testUtils.BaseAndroidTestCase;
import de.bitdroid.flooding.testUtils.PrefsRenamingDelegatingContext;
import de.bitdroid.flooding.testUtils.SharedPreferencesHelper;
import de.bitdroid.flooding.utils.Log;

public class GcmReceiverTest extends BaseAndroidTestCase {

	private static final String PREFIX = GcmReceiverTest.class.getSimpleName();
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String
			CLIENTID = "someClientId",
			EPL_STMT = "select * from *",
			EVENTID = "someEventId";

	private MockWebServer server;
	private BroadcastReceiver eventReceiver;
	private int receiverCount;
	private CepManager manager;


	@Override
	public void beforeClass() {
		setContext(new PrefsRenamingDelegatingContext(getContext(), PREFIX));
	}


	@Override
	public void beforeTest() {
		SharedPreferencesHelper.clearAll((PrefsRenamingDelegatingContext) getContext());

		this.server = new MockWebServer();
		this.manager = CepManagerFactory.createCepManager(getContext());

		this.receiverCount = 0;
		this.eventReceiver = new BaseEventReceiver() {
			@Override
			protected void onReceive(Context context, String eplStmt, String eventId) {
				assertEquals(EPL_STMT, eplStmt);
				assertEquals(EVENTID, eventId);
				receiverCount++;
			}
		};

		getContext().registerReceiver(
				eventReceiver,
				new IntentFilter("de.bitdroid.flooding.ods.cep.ACTION_EVENT_RECEIVED"));
	}


	@Override
	public void tearDown() throws Exception {
		this.server.shutdown();
		getContext().unregisterReceiver(eventReceiver);
	}


	/*
	public void testValidEvent() throws Exception {
		Map<String, Object> clientMap = new HashMap<String, Object>();
		clientMap.put("clientId", CLIENTID);
		JsonNode node = mapper.valueToTree(clientMap);

		server.enqueue(new MockResponse().setBody(((Object) node).toString()));
		server.enqueue(new MockResponse().setResponseCode(404));
		server.play();
		manager.setCepServerName(server.getUrl("").toString());
		manager.registerEplStmt(EPL_STMT);

		Thread.sleep(300);

		new GcmReceiver().handle(getContext(), getIntent());

		Thread.sleep(300);

		assertEquals(1, receiverCount);
		assertEquals(2, server.getRequestCount());
		assertTrue(server.takeRequest().getPath().contains("register"));
		assertTrue(server.takeRequest().getPath().contains("unregister"));
	}

*/

	public void testInvalidRequest() throws Exception {
		server.enqueue(new MockResponse().setResponseCode(500));
		server.play();
		manager.setCepServerName(server.getUrl("").toString());
		Log.debug("server url = " + server.getUrl("").toString());
		Log.debug("manager url = " + manager.getCepServerName());

		new GcmReceiver().handle(getContext(), getIntent());

		Thread.sleep(1000);

		assertEquals(0, receiverCount);
		assertEquals(1, server.getRequestCount());
		assertTrue(server.takeRequest().getPath().contains("unregister"));
	}


	private Intent getIntent() {
		Intent intent = new Intent();
		intent.putExtra("client", CLIENTID);
		intent.putExtra("event", EVENTID);
		return intent;
	}

}
