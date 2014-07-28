package de.bitdroid.flooding.ods.cep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.test.RenamingDelegatingContext;
import android.test.ServiceTestCase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class GcmIntentServiceTest extends ServiceTestCase<GcmIntentService> {

	private final static String
			EPL_STMT = "select * from *",
			CLIENT_ID = "someId";

	private static final ObjectMapper mapper = new ObjectMapper();

	private int counter = 0;

	public GcmIntentServiceTest() {
		super(GcmIntentService.class);
	}


	@Override
	public void setUp() {
		counter = 0;
	}


	public void testRegister() throws Exception {
		// prepare response
		Map<String, Object> clientIdMap = new HashMap<String, Object>();
		clientIdMap.put("clientId", CLIENT_ID);
		JsonNode json = mapper.valueToTree(clientIdMap);

		// start mock server
		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setBody(json.toString()));
		server.play();
		URL serverUrl = server.getUrl("");
		CepManager.getInstance(getContext()).setCepServerName(serverUrl.toString());

		// register receiver
		getContext().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String eplStmt = intent.getStringExtra(GcmIntentService.EXTRA_EPL_STMT);
					String errorMsg = intent.getStringExtra(GcmIntentService.EXTRA_ERROR_MSG);
					String clientId = intent.getStringExtra(GcmIntentService.EXTRA_SERVICE_CLIENTID);
					boolean register = intent.getBooleanExtra(GcmIntentService.EXTRA_REGISTER, false);

					assertEquals(EPL_STMT, eplStmt);
					assertNull(errorMsg);
					assertEquals(CLIENT_ID, clientId);
					assertTrue(register);

					counter++;
				}
			}, new IntentFilter("de.bitdroid.flooding.ods.cep.ACTION_GCM_FINISH"));

		// start service
		Intent intent = new Intent(getContext(), GcmIntentService.class);
		intent.putExtra(GcmIntentService.EXTRA_EPL_STMT, EPL_STMT);
		intent.putExtra(GcmIntentService.EXTRA_REGISTER, true);
		getContext().startService(intent);

		// wait for service to finish
		Thread.sleep(1000);

		assertEquals(1, server.getRequestCount());
		RecordedRequest request = server.takeRequest();
		assertTrue(request.getPath().contains("cep/gcm/register"));
		assertTrue(request.getPath().contains("eplStmt=" + URLEncoder.encode(EPL_STMT)));
		assertEquals(1, counter);

		server.shutdown();
	}

}
