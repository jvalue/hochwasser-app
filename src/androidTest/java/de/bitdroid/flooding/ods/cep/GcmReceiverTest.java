package de.bitdroid.flooding.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import java.util.HashMap;
import java.util.Map;

import de.bitdroid.flooding.utils.SharedPreferencesHelper;

public class GcmReceiverTest extends AndroidTestCase {

	private static final ObjectMapper mapper = new ObjectMapper();
	private static final String
			CLIENTID = "someClientId",
			EPL_STMT = "select * from *",
			EVENTID = "someEventId";

	private MockWebServer server;
	private Context context;
	private BroadcastReceiver eventReceiver;
	private int receiverCount;
	private CepManager manager;


	@Override
	public void setUp() {
		this.server = new MockWebServer();
		this.context = new RenamingDelegatingContext(
				getContext(),
				"GcmReceiverTest");
		SharedPreferencesHelper.clearAll(context);

		this.manager = CepManager.getInstance(context);

		this.receiverCount = 0;
		this.eventReceiver = new BaseEventReceiver() {
			@Override
			protected void onReceive(Context context, String eplStmt, String eventId) {
				assertEquals(EPL_STMT, eplStmt);
				assertEquals(EVENTID, eventId);
				receiverCount++;
			}
		};

		this.context.registerReceiver(
				eventReceiver,
				new IntentFilter("de.bitdroid.flooding.ods.cep.ACTION_EVENT_RECEIVED"));
	}


	@Override
	public void tearDown() throws Exception {
		this.server.shutdown();
		this.context.unregisterReceiver(eventReceiver);
	}


	public void testValidEvent() throws Exception {
		Map<String, Object> clientMap = new HashMap<String, Object>();
		clientMap.put("clientId", CLIENTID);
		JsonNode node = mapper.valueToTree(clientMap);

		server.enqueue(new MockResponse().setBody(((Object) node).toString()));
		server.play();
		manager.setCepServerName(server.getUrl("").toString());
		manager.registerEplStmt(EPL_STMT);

		Thread.sleep(1000);

		new GcmReceiver().handle(getContext(), getIntent());

		Thread.sleep(1000);

		assertEquals(1, receiverCount);
		assertEquals(1, server.getRequestCount());
		assertTrue(server.takeRequest().getPath().contains("register"));
	}


	public void testInvalidEvent() throws Exception {
		server.play();
		manager.setCepServerName(server.getUrl("").toString());

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
