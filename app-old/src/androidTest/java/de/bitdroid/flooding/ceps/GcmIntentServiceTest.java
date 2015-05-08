package de.bitdroid.flooding.ceps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.ServiceTestCase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GcmIntentServiceTest extends ServiceTestCase<GcmIntentService> {

	private final static String
			CLIENT_ID = "someId";
	private final static Rule rule = new Rule.Builder("somePath")
			.parameter("key1", "value1")
			.parameter("key2", "value2")
			.build();

	private static final ObjectMapper mapper = new ObjectMapper();

	private int counter = 0;
	private MockWebServer server;
	private BroadcastReceiver receiver;
	private boolean register;

	public GcmIntentServiceTest() {
		super(GcmIntentService.class);
	}


	@Override
	public void setUp() {
		this.counter = 0;
		this.server = new MockWebServer();

		// register receiver
		this.receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Rule rule = intent.getParcelableExtra(GcmIntentService.EXTRA_RULE);
				String errorMsg = intent.getStringExtra(GcmIntentService.EXTRA_ERROR_MSG);
				String clientId = intent.getStringExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID);
				boolean register = intent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false);

				assertEquals(GcmIntentServiceTest.rule, rule);
				assertNull(errorMsg);
				assertEquals(CLIENT_ID, clientId);
				assertEquals(GcmIntentServiceTest.this.register, register);

				counter++;
			}
		};
		getContext().registerReceiver(
				receiver,
				new IntentFilter("de.bitdroid.ods.cep.ACTION_GCM_FINISH"));

		RuleManagerFactory.setRuleManager(new RuleManagerImpl(getContext(), new RuleDb(getContext())));
	}


	@Override
	public void tearDown() throws Exception {
		this.server.shutdown();
		this.server = null;

		getContext().unregisterReceiver(receiver);
		this.receiver = null;
	}


	public void testRegister() throws Exception {
		this.register = true;

		// prepare response
		Map<String, Object> clientIdMap = new HashMap<String, Object>();
		clientIdMap.put("clientId", CLIENT_ID);
		JsonNode json = mapper.valueToTree(clientIdMap);

		// start mock server
		server.enqueue(new MockResponse().setBody(((Object) json).toString()));
		server.play();
		URL serverUrl = server.getUrl("");
		RuleManagerFactory.createRuleManager(getContext()).setCepServerName(serverUrl.toString());

		// start service
		Intent intent = new Intent(getContext(), GcmIntentService.class);
		intent.putExtra(GcmIntentService.EXTRA_RULE, rule);
		intent.putExtra(GcmIntentService.EXTRA_REGISTER, true);
		getContext().startService(intent);

		// wait for service to finish
		Thread.sleep(300);

		assertEquals(1, server.getRequestCount());
		RecordedRequest request = server.takeRequest();
		assertTrue(request.getPath().contains("somePath"));
		assertTrue(request.getPath().contains("key1=value1"));
		assertTrue(request.getPath().contains("key2=value2"));
		assertEquals(1, counter);
	}


	public void testUnregister() throws Exception {
		this.register = false;

		// start mock server
		server.enqueue(new MockResponse());
		server.play();
		URL serverUrl = server.getUrl("");
		RuleManagerFactory.createRuleManager(getContext()).setCepServerName(serverUrl.toString());

		// start service
		Intent intent = new Intent(getContext(), GcmIntentService.class);
		intent.putExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID, CLIENT_ID);
		intent.putExtra(GcmIntentService.EXTRA_RULE, rule);
		intent.putExtra(GcmIntentService.EXTRA_REGISTER, false);
		getContext().startService(intent);

		// wait for service to finish
		Thread.sleep(300);

		assertEquals(1, server.getRequestCount());
		RecordedRequest request = server.takeRequest();
		assertTrue(request.getPath().contains("cep/unregister"));
		assertTrue(request.getPath().contains("clientId=" + CLIENT_ID));
		assertEquals(1, counter);
	}

}
