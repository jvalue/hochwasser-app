package de.bitdroid.flooding.testUtils;

import android.test.AndroidTestCase;

public class BaseAndroidTestCase extends AndroidTestCase {

	private boolean setup = false;

	@Override
	public final void setUp() {
		if (!setup) {
			beforeClass();
			setup = true;
		}
		beforeTest();
	}


	public void beforeTest() { }
	public void beforeClass() { }

}
