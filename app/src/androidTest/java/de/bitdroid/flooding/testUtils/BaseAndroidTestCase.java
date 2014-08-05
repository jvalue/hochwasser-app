package de.bitdroid.flooding.testUtils;

import android.test.AndroidTestCase;

public class BaseAndroidTestCase extends AndroidTestCase {

	private boolean setup = false;

	@Override
	public final void setUp() throws Exception {
		if (!setup) {
			beforeClass();
			setup = true;
		}
		beforeTest();
	}


	public void beforeTest() throws Exception { }
	public void beforeClass() throws Exception { }

}
