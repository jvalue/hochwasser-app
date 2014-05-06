package de.bitdroid.flooding.utils;

final class AndroidLogHandler implements LogHandler {

	private final static String LOG_PREFIX = "MailDroid";
	
	@Override
	public void debug(String msg) {
		android.util.Log.d(LOG_PREFIX, prefix(msg));
	}

	@Override
	public void info(String msg) {
		android.util.Log.i(LOG_PREFIX, prefix(msg));
	}

	@Override
	public void warning(String msg) {
		android.util.Log.w(LOG_PREFIX, prefix(msg));
	}

	@Override
	public void error(String msg) {
		android.util.Log.e(LOG_PREFIX, prefix(msg));
	}

	private String prefix(String msg) {
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
