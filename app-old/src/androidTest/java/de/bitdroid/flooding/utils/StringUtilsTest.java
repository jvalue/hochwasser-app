package de.bitdroid.flooding.utils;

import junit.framework.TestCase;


public final class StringUtilsTest extends TestCase {

	public void testProperCase() {
		String[] rawStrings = { "hello WORLD", "FOOBAR", "A-B-cc", "hELLO" };
		String[] capStrings = { "Hello World", "Foobar", "A-B-Cc", "Hello" };

		for (int i = 0; i < rawStrings.length; i++) {
			assertEquals(StringUtils.toProperCase(rawStrings[i]), capStrings[i]);
		}
	}

}
