package de.bitdroid.flooding.utils;

import java.util.LinkedList;
import java.util.List;


public final class Log {

	private Log() { }

	static {
		logHandlers = new LinkedList<LogHandler>();
		addHandler(new AndroidLogHandler());
	}

	private final static List<LogHandler> logHandlers;

	public static synchronized void addHandler(LogHandler handler) {
		logHandlers.add(handler);
	}

	public static synchronized boolean removeHandler(LogHandler handler) {
		return logHandlers.remove(handler);
	}


	public static synchronized void debug(String msg) {
		for (LogHandler handler : logHandlers)
			handler.debug(msg);
	}

	public static synchronized void info(String msg) {
		for (LogHandler handler : logHandlers)
			handler.info(msg);
	}

	public static synchronized void warning(String msg) {
		for (LogHandler handler : logHandlers)
			handler.warning(msg);
	}

	public static synchronized void error(String msg) {
		for (LogHandler handler : logHandlers)
			handler.error(msg);
	}
}
