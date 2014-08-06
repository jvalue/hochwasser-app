package de.bitdroid.utils;

import junit.framework.TestCase;

public class LogTest extends TestCase {

    private static final String MSG = "test";
    private static final Throwable ERROR = new RuntimeException(MSG);

    public void testLog() {
        Log.debug(MSG);
        Log.debug(MSG, ERROR);
        Log.info(MSG);
        Log.info(MSG, ERROR);
        Log.warning(MSG);
        Log.warning(MSG, ERROR);
        Log.error(MSG);
        Log.error(MSG, ERROR);
    }
}
