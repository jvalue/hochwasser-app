package de.bitdroid.flooding.ods.data;

import junit.framework.TestCase;


public final class OdsSourceTest extends TestCase {

	public void testEqualsHashCode() {
		DummySource s1 = new DummySource();
		DummySource s2 = new DummySource();
		assertTrue(s1.equals(s2));
		assertTrue(s1.hashCode() == s2.hashCode());

		DummySource s3 = new DummySource("foobar");
		assertFalse(s1.equals(s3));
		assertFalse(s1.hashCode() == s3.hashCode());
	}


	public void testUriConversion() {
		DummySource s1 = new DummySource();
		DummySource s2 = (DummySource) OdsSource.fromUri(s1.toUri());
		DummySource s3 = (DummySource) OdsSource.fromUri(s1.toSyncUri());

		assertEquals(s1, s2);
		assertEquals(s1, s3);
	}


	public void testSyncUri() {
		DummySource s = new DummySource();
		
		assertTrue(OdsSource.isSyncUri(s.toSyncUri()));
		assertFalse(OdsSource.isSyncUri(s.toUri()));
	}

}
