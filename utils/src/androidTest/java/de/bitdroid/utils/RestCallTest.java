package de.bitdroid.utils;

import android.test.AndroidTestCase;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;


public final class RestCallTest extends AndroidTestCase {

	public void testSuccess() throws Exception {
		String restResult = "someContent";
		String somePath = "somePath";

		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setBody(restResult));
		server.play();

		RestCall call = new RestCall.Builder(
				RestCall.RequestType.GET,
				server.getUrl("").toString())
			.path(somePath)
			.build();

		assertEquals(restResult, call.execute());
		assertTrue(server.takeRequest().getPath().contains(somePath));

		server.shutdown();
	}


	public void testFailure() throws Exception {
		int code = 500;

		MockWebServer server = new MockWebServer();
		server.enqueue(new MockResponse().setResponseCode(code));
		server.play();

		try {
			new RestCall.Builder(
					RestCall.RequestType.POST,
					server.getUrl("").toString())
				.build()
				.execute();

		} catch (RestException re) {
			assertEquals(code, re.getCode());
			return;

		} finally {
			server.shutdown();
		}

		fail();
	}

}
