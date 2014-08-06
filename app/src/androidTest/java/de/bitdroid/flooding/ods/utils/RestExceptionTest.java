package de.bitdroid.flooding.ods.utils;

import junit.framework.TestCase;

import java.io.IOException;

import de.bitdroid.utils.RestException;

public class RestExceptionTest extends TestCase {

    public void testGet() {
        int code = 501;
        RestException e1 = new RestException(code, "boom");

        assertEquals(code, e1.getCode());
        assertNull(e1.getCause());
        assertTrue(e1.getMessage().contains("boom"));

        IOException cause = new IOException("boom");
        RestException e2 = new RestException(cause);

        assertEquals(cause, e2.getCause());
        assertEquals(RestException.UNSET, e2.getCode());
    }

}
