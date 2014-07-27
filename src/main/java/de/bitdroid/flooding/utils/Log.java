package de.bitdroid.flooding.utils;

public final class Log {

    private Log() { }


    private final static String LOG_PREFIX = "Flooding";

    public static void debug(String msg) {
        android.util.Log.d(LOG_PREFIX, prefix(msg));
    }

    public static void debug(String msg, Throwable error) {
        android.util.Log.d(LOG_PREFIX, prefix(msg), error);
    }


    public static void info(String msg) {
        android.util.Log.i(LOG_PREFIX, prefix(msg));
    }

    public static void info(String msg, Throwable error) {
        android.util.Log.i(LOG_PREFIX, prefix(msg), error);
    }


    public static void warning(String msg) {
        android.util.Log.w(LOG_PREFIX, prefix(msg));
    }

    public static void warning(String msg, Throwable error) {
        android.util.Log.w(LOG_PREFIX, prefix(msg), error);
    }


    public static void error(String msg) {
        android.util.Log.e(LOG_PREFIX, prefix(msg));
    }

    public static void error(String msg, Throwable error) {
        android.util.Log.e(LOG_PREFIX, prefix(msg), error);
    }


    private static String prefix(String msg) {
        String callerName = Thread.currentThread().getStackTrace()[5].getClassName();

        try {
            Class<?> caller = Class.forName(callerName);
            String name = caller.getSimpleName();
            // check for inner classes (which don't have a simple name)
            if (name.equals(""))
                name = caller.getName();

            return name + ": " + msg;

        } catch (ClassNotFoundException e) {
            android.util.Log.w(LOG_PREFIX, "Failed to create class from name");
        }
        return LOG_PREFIX;
    }

}
