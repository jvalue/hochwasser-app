package de.bitdroid.flooding.ods.utils;

import android.test.AndroidTestCase;

import de.bitdroid.flooding.utils.RestCall;
import de.bitdroid.flooding.utils.RestException;


public final class RestCallTest extends AndroidTestCase {

	public void testBasicConnection() throws RestException {

		RestCall call = new RestCall.Builder(
				RestCall.RequestType.GET, 
				"http://faui2o2f.cs.fau.de:8080")
			.path("open-data-service")
			.path("ods")
			.path("de")
			.path("pegelonline")
			.path("stationsFlat")
			.build();

		String result = call.execute();

		assertTrue("Result was null", result != null);
	}

}
