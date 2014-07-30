package de.bitdroid.flooding.utils;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;


public final class AssertTest extends TestCase {

	public void testAssertNotNullOne() {
		try {
			Object object = null;
			Assert.assertNotNull(object);
			fail();
		} catch (NullPointerException npe) { }
	}


	public void testAssertNotNullMultiple() {
		try {
			Assert.assertNotNull(new Object(), null);
			fail();
		} catch (NullPointerException npe) { }
	}


	public void testAssertTrue() {
		try {
			Assert.assertTrue(false);
			fail();
		} catch (IllegalArgumentException e) { }
	}


	public void testAssertTrueMsg() {
		try {
			Assert.assertTrue(false, "error");
			fail();
		} catch (IllegalArgumentException iae) {
			assertEquals(iae.getMessage(), "error");
		}
	}


	public void testAssertFalse() {
		try {
			Assert.assertFalse(true, "error");
			fail();
		} catch (IllegalArgumentException e) { }
	}


	public void testAssertEquals() {
		testAssertEquals("one", "two");
		testAssertEquals("one", null);
		testAssertEquals(null, "two");

		Assert.assertEquals(null, null, "error");
		Assert.assertEquals("dummy", "dummy", "error");
	}


	private void testAssertEquals(Object o1, Object o2) {
		try {
			Assert.assertEquals(o1, o2, "error");
			fail();
		} catch (IllegalArgumentException iae) {
			assertEquals(iae.getMessage(), "error");
		}
	}


	public void testAssertValidIdx() {
		try {
			Assert.assertValidIdx(new LinkedList<String>(), -1);
			fail();
		} catch(IndexOutOfBoundsException iae) { }

		try {
			Assert.assertValidIdx(new LinkedList<String>(), 1);
			fail();
		} catch(IndexOutOfBoundsException iae) { }

		List<String> values = new LinkedList<String>();
		values.add("dummy");
		Assert.assertValidIdx(values, 0);
	}

}
